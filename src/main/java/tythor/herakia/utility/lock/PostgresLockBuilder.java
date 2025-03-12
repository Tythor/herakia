package tythor.herakia.utility.lock;

import lombok.SneakyThrows;
import tythor.herakia.exception.LockedException;
import tythor.herakia.component.AdvisoryLockManager;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class PostgresLockBuilder extends AbstractLockBuilder {
    private final AdvisoryLockManager advisoryLockManager;

    public PostgresLockBuilder(AdvisoryLockManager advisoryLockManager) {
        this.advisoryLockManager = advisoryLockManager;
    }

    @Override
    protected void executeRunnable() throws LockedException {
        Lock lock = acquireLock();

        try {
            runnable.run();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {}
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T executeSupplier() throws LockedException {
        Lock lock = acquireLock();

        try {
            return (T) supplier.get();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {}
        }
    }

    @SneakyThrows
    private Lock acquireLock() throws LockedException {
        Lock lock = advisoryLockManager.obtain(lockKey);

        switch (lockType) {
            case LOCK -> lock.lock();
            case TRY_LOCK -> {
                if (leaseTime != ChronoUnit.FOREVER.getDuration()) throw new IllegalArgumentException("Lease time not supported.");
                if (!lock.tryLock(convertToNanos(waitTime), TimeUnit.NANOSECONDS)) throw new LockedException();
            }
        }

        return lock;
    }
}
