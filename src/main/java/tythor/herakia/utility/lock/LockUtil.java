package tythor.herakia.utility.lock;

import tythor.herakia.component.AdvisoryLockManager;
import tythor.herakia.configuration.HerakiaHazelcastConfig;
import tythor.herakia.exception.LockedException;
import com.hazelcast.map.IMap;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.RedisUtil;
import tythor.herakia.utility.SignatureUtil;
import tythor.herakia.utility.SpringUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
@Deprecated
public class LockUtil {
    public void executeLocked(long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        String lockKey = SignatureUtil.getSignature();
        executeLocked(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public <K> void executeLocked(K lockKey, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        IMap<K, ?> lockMap = HazelcastUtil.getMap(HerakiaHazelcastConfig.LOCK_MAP);
        executeLocked(lockMap, lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    @SneakyThrows
    public <K> void executeLocked(IMap<K, ?> lockMap, K lockKey, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        if (!lockMap.tryLock(lockKey, waitTimeSeconds, TimeUnit.SECONDS, leaseTimeSeconds, TimeUnit.SECONDS)) throw new LockedException();

        try {
            task.run();
        } finally {
            try {
                lockMap.unlock(lockKey);
            } catch (Exception e) {}
        }
    }

    public <T> T executeLocked(long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        String lockKey = SignatureUtil.getSignature();
        return executeLocked(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public <T, K> T executeLocked(K lockKey, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        IMap<K, ?> lockMap = HazelcastUtil.getMap(HerakiaHazelcastConfig.LOCK_MAP);
        return executeLocked(lockMap, lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    @SneakyThrows
    public <T, K> T executeLocked(IMap<K, ?> lockMap, K lockKey, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        if (!lockMap.tryLock(lockKey, waitTimeSeconds, TimeUnit.SECONDS, leaseTimeSeconds, TimeUnit.SECONDS)) throw new LockedException();

        try {
            return task.get();
        } finally {
            try {
                lockMap.unlock(lockKey);
            } catch (Exception e) {}
        }
    }

    public <T, K> T executeLockedForce(IMap<K, ?> lockMap, K lockKey, Supplier<T> task) {
        try {
            lockMap.lock(lockKey);
            return task.get();
        } finally {
            lockMap.unlock(lockKey);
        }
    }

    public <T> T executeLockedRedis(long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        String lockKey = SignatureUtil.getSignature();
        return executeLockedRedis(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public <T> T executeLockedRedis(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        RLock lock = RedisUtil.getClient().getFairLock(lockKey);
        return executeLockedRedis(lock, waitTimeSeconds, leaseTimeSeconds, task);
    }

    @SneakyThrows
    private <T> T executeLockedRedis(RLock lock, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        if (!lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS)) throw new LockedException();

        try {
            return task.get();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {}
        }
    }

    public void executeLockedPostgres(long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        String lockKey = SignatureUtil.getSignature();
        executeLockedPostgres(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public void executeLockedPostgres(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        Lock lock = SpringUtil.getBean(AdvisoryLockManager.class).obtain(lockKey);
        executeLockedPostgres(lock, waitTimeSeconds, leaseTimeSeconds, task);
    }

    @SneakyThrows
    private void executeLockedPostgres(Lock lock, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        if (leaseTimeSeconds != 0) throw new IllegalArgumentException("Lease time not supported.");

        if (!lock.tryLock(waitTimeSeconds, TimeUnit.SECONDS)) throw new LockedException();

        try {
            task.run();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {}
        }
    }
}
