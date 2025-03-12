package tythor.herakia.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.SneakyThrows;
import tythor.herakia.exception.LockedException;

import java.util.concurrent.TimeUnit;

public class HazelcastLockBuilder extends AbstractLockBuilder {
    private IMap<Object, Object> lockMap;

    public HazelcastLockBuilder(HazelcastInstance hazelcastInstance) {
        this.lockMap = hazelcastInstance.getMap("lockMap");
    }

    @SuppressWarnings("unchecked")
    public class LockMapStep extends LockKeyStep {
        public LockKeyStep lockMap(IMap<?, ?> lockMap) {
            HazelcastLockBuilder.this.lockMap = (IMap<Object, Object>) lockMap;
            return new LockKeyStep();
        }
    }

    @Override
    protected void executeRunnable() throws LockedException {
        acquireLock();

        try {
            runnable.run();
        } finally {
            try {
                lockMap.unlock(lockKey);
            } catch (Exception e) {}
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T executeSupplier() throws LockedException {
        acquireLock();

        try {
            return (T) supplier.get();
        } finally {
            try {
                lockMap.unlock(lockKey);
            } catch (Exception e) {}
        }
    }

    @SneakyThrows
    private void acquireLock() throws LockedException {
        switch (lockType) {
            case LOCK -> lockMap.lock(lockKey);
            case TRY_LOCK -> {
                if (!lockMap.tryLock(lockKey, convertToNanos(waitDuration), TimeUnit.NANOSECONDS, convertToNanos(leaseDuration), TimeUnit.NANOSECONDS)) throw new LockedException();
            }
        }
    }
}
