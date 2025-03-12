package tythor.herakia.utility.lock;

import com.hazelcast.map.IMap;
import lombok.SneakyThrows;
import tythor.herakia.exception.LockedException;
import tythor.herakia.utility.HazelcastUtil;

import java.util.concurrent.TimeUnit;

public class HazelcastLockBuilder extends AbstractLockBuilder {
    private IMap<Object, Object> lockMap = HazelcastUtil.getMap("lockMap");

    @SuppressWarnings("unchecked")
    public class LockMapStep extends LockKeyStep {
        public LockKeyStep lockMap(IMap<?, ?> lockMap) {
            HazelcastLockBuilder.this.lockMap = (IMap<Object, Object>) lockMap;
            return new LockKeyStep();
        }
    }

    @Override
    protected void executeRunnable() throws LockedException {
        doLock();

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
        doLock();

        try {
            return (T) supplier.get();
        } finally {
            try {
                lockMap.unlock(lockKey);
            } catch (Exception e) {}
        }
    }

    @SneakyThrows
    private void doLock() throws LockedException {
        switch (lockType) {
            case LOCK -> lockMap.lock(lockKey);
            case TRY_LOCK -> {
                if (!lockMap.tryLock(lockKey, convertToNanos(waitTime), TimeUnit.NANOSECONDS, convertToNanos(leaseTime), TimeUnit.NANOSECONDS)) throw new LockedException();
            }
        }
    }
}
