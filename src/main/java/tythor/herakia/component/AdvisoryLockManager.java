package tythor.herakia.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A manager for Postgres advisory locks that maintains a pool of connections using HikariCP.
 * This lock ensures that only one thread can perform an operation associated with a specific key at a time.
 * <p>
 * Each {@link AdvisoryLock} obtained from the manager is an implementation of {@link Lock} and backed by a {@link ReentrantLock}.
 * <p>
 * Internally, the lock is obtained from Postgres by executing <code>pg_try_advisory_xact_lock(id)</code> and released by ending the transaction.
 * <p>
 * Internally, a map of key-value pairs is maintained, where the key is the lock identifier, and the value is the associated database connection.
 * <p>
 * <b>Note:</b> Does not participate in existing {@link org.springframework.transaction.annotation.Transactional @Transactional} transactions.
 * <p>
 * <a href="https://www.postgresql.org/docs/current/explicit-locking.html#ADVISORY-LOCKS">https://www.postgresql.org/docs/current/explicit-locking.html#ADVISORY-LOCKS</a>
 * <p>
 * <a href="https://www.postgresql.org/docs/current/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS">https://www.postgresql.org/docs/current/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdvisoryLockManager {
    private final Map<Object, Connection> connectionMap = new ConcurrentHashMap<>();
    private final Map<Object, AdvisoryLock> lockMap = new ConcurrentHashMap<>();
    private final HikariDataSource hikariDataSource;

    private HikariDataSource advisoryHikariDataSource;

    @PostConstruct
    public void init() {
        advisoryHikariDataSource = createNewHikariDataSource(hikariDataSource);
    }

    @PreDestroy
    public void destroy() {
        advisoryHikariDataSource.close();
    }

    private HikariDataSource createNewHikariDataSource(HikariDataSource hikariDataSource) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariDataSource.copyStateTo(hikariConfig);
        hikariConfig.setPoolName(hikariConfig.getPoolName().replace("Hikari", "Hikari-Lock"));
        hikariConfig.setMaximumPoolSize(10000);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setAutoCommit(false);
        return new HikariDataSource(hikariConfig);
    }

    public AdvisoryLock obtain(Object key) {
        return lockMap.computeIfAbsent(key, k -> new AdvisoryLock(k, connectionMap, advisoryHikariDataSource));
    }

    public static class AdvisoryLock implements Lock {
        private ReentrantLock delegate = new ReentrantLock();
        private final Object key;
        private final Map<Object, Connection> connectionMap;
        private final DataSource dataSource;
        private final JdbcTemplate jdbcTemplate;

        public AdvisoryLock(Object key, Map<Object, Connection> connectionMap, DataSource dataSource) {
            this.key = key;
            this.connectionMap = connectionMap;
            this.dataSource = dataSource;
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        /**
         * <b>Note:</b> This method does not use {@code pg_advisory_xact_lock} in order to avoid holding an open connection.
         */
        @Override
        public void lock() {
            while (!tryLock());
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            while (!tryLock()) {
                if (Thread.interrupted()) throw new InterruptedException();
            }
        }

        @SneakyThrows
        @Override
        public boolean tryLock() {
            if (!delegate.tryLock(1, TimeUnit.MILLISECONDS)) return false;
//            if (!delegate.tryLock(100, TimeUnit.MICROSECONDS)) return false;

            if (delegate.getHoldCount() == 1) {
                boolean acquired = doLock();
                if (!acquired) delegate.unlock();
                return acquired;
            } else {
                return true;
            }
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) {
            long startTime = System.nanoTime();
            while (!tryLock()) {
                if (System.nanoTime() - startTime > unit.toNanos(time)) return false;
            }
            return true;
        }

        private boolean doLock() {
            if (connectionMap.containsKey(key)) return false;

            try {
                String query = "SELECT pg_try_advisory_xact_lock(?)";
                Connection connection = dataSource.getConnection();

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, key.hashCode());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next() && resultSet.getBoolean(1)) {
                            connectionMap.put(key, connection);
                            return true;
                        }
                    }
                }

                connection.close();
            } catch (SQLException e) {}

            return false;
        }

        @Override
        public void unlock() {
            if (delegate.isHeldByCurrentThread() && delegate.getHoldCount() == 1) {
                if (!doUnlock()) throw new IllegalMonitorStateException();
            }
            delegate.unlock();
        }

        private boolean doUnlock() {
            Connection connection = connectionMap.get(key);
            if (connection == null) return false;

            try (connection) {
                connection.commit();
                return true;
            } catch (SQLException e) {
            } finally {
                connectionMap.remove(key, connection);
            }

            return false;
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public boolean isHeldByCurrentThread() {
            return delegate.isHeldByCurrentThread();
        }

        public boolean isLocked() {
            if (delegate.isLocked()) return true;

            String sql = "SELECT COUNT(*) FROM pg_locks " +
                "WHERE database = (SELECT oid FROM pg_database WHERE datname = current_database()) " +
                "AND locktype = 'advisory' " +
                "AND objid = ?";

            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, key.hashCode());
            return sqlRowSet.next() && sqlRowSet.getInt(1) > 0;
        }

        /**
         * Will attempt to unlock normally if lock is held by this thread.
         */
        public boolean forceUnlock() {
            if (delegate.isHeldByCurrentThread()) {
                unlock();
                return true;
            }

            String sql = "SELECT pg_terminate_backend(pid) FROM pg_locks " +
                "WHERE database = (SELECT oid FROM pg_database WHERE datname = current_database()) " +
                "AND locktype = 'advisory' " +
                "AND objid = ?";

            try {
                SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, key.hashCode());
                return sqlRowSet.next() && sqlRowSet.getBoolean(1);
            } catch (Exception e) {
              return false;
            } finally {
                connectionMap.remove(key);
                delegate = new ReentrantLock();
            }
        }
    }
}
