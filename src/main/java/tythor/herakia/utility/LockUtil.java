package tythor.herakia.utility;

import tythor.herakia.exception.LockedException;
import com.hazelcast.map.IMap;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class LockUtil {
    private String getLockKey() {
        // StackWalker doesn't work in static methods
        /*List<String> skippedClasses = List.of("org.springframework", "$$SpringCGLIB$$", this.getClass().getName());
        StackWalker.StackFrame stackFrame = StackWalker.getInstance().walk(x -> x.filter(y -> skippedClasses.stream().noneMatch(z -> y.getClassName().contains(z))).findFirst()).orElseThrow();
        String lockKey = stackFrame.getClassName() + "." + stackFrame.getMethodName().replaceAll("lambda\\$|\\$1", "");*/

        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        String lockKey = simpleClassName + "." + methodName;
        return lockKey;
    }

    public void executeLocked(long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        String lockKey = getLockKey();
        executeLocked(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public <K> void executeLocked(K lockKey, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        IMap<K, ?> lockMap = HazelcastUtil.getMap("lockMap");
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
        String lockKey = getLockKey();
        return executeLocked(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public <T, K> T executeLocked(K lockKey, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> task) throws LockedException {
        IMap<K, ?> lockMap = HazelcastUtil.getMap("lockMap");
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
        String lockKey = getLockKey();
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
        String lockKey = getLockKey();
        executeLockedPostgres(lockKey, waitTimeSeconds, leaseTimeSeconds, task);
    }

    public void executeLockedPostgres(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
        AdvisoryLockManager.AdvisoryLock lock = SpringUtil.getBean(AdvisoryLockManager.class).obtain(lockKey);
        executeLockedPostgres(lock, waitTimeSeconds, leaseTimeSeconds, task);
    }

    @SneakyThrows
    private void executeLockedPostgres(AdvisoryLockManager.AdvisoryLock lock, long waitTimeSeconds, long leaseTimeSeconds, Runnable task) throws LockedException {
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
