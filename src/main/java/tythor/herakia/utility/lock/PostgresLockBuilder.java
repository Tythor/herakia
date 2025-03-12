package tythor.herakia.utility.lock;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import tythor.herakia.component.AdvisoryLockManager;
import tythor.herakia.exception.LockedException;
import tythor.herakia.exception.LockedHttpException;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
public class PostgresLockBuilder extends AbstractLockBuilder {
    private final AdvisoryLockManager advisoryLockManager;

    @Override
    protected void executeRunnable() {
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
    protected <T> T executeSupplier() {
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
    private Lock acquireLock() {
        Lock lock = advisoryLockManager.obtain(lockKey);

        switch (lockType) {
            case LOCK -> lock.lock();
            case TRY_LOCK -> {
                if (leaseDuration != ChronoUnit.FOREVER.getDuration()) throw new IllegalArgumentException("Lease time not supported.");

                if (!lock.tryLock(super.convertToNanos(waitDuration), TimeUnit.NANOSECONDS)) {
                    if (super.throwUnchecked) {
                        throw new LockedHttpException();
                    } else {
                        throw new LockedException();
                    }
                }
            }
        }

        return lock;
    }
}
