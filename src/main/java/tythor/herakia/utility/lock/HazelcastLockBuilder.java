package tythor.herakia.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.SneakyThrows;
import tythor.herakia.configuration.HerakiaHazelcastConfig;
import tythor.herakia.exception.LockedException;
import tythor.herakia.exception.LockedHttpException;

import java.util.concurrent.TimeUnit;

public class HazelcastLockBuilder extends AbstractLockBuilder {
    private IMap<Object, Object> lockMap;

    public HazelcastLockBuilder(HazelcastInstance hazelcastInstance) {
        this.lockMap = hazelcastInstance.getMap(HerakiaHazelcastConfig.LOCK_MAP);
    }

    @SuppressWarnings("unchecked")
    public class LockMapStep extends LockKeyStep {
        public LockKeyStep lockMap(IMap<?, ?> lockMap) {
            HazelcastLockBuilder.this.lockMap = (IMap<Object, Object>) lockMap;
            return new LockKeyStep();
        }
    }

    @Override
    protected void executeRunnable() {
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
    protected <T> T executeSupplier() {
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
    private void acquireLock() {
        switch (lockType) {
            case LOCK -> lockMap.lock(lockKey);
            case TRY_LOCK -> {
                if (!lockMap.tryLock(lockKey, super.convertToNanos(waitDuration), TimeUnit.NANOSECONDS, super.convertToNanos(leaseDuration), TimeUnit.NANOSECONDS)) {
                    if (super.throwUnchecked) {
                        throw new LockedHttpException();
                    } else {
                        throw new LockedException();
                    }
                }
            }
        }
    }
}
