package tythor.herakia.utility.lock;

import tythor.herakia.exception.LockedException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractLockBuilder {
    protected Object lockKey = getDefaultLockKey();
    protected LockType lockType;
    protected Duration waitTime = Duration.ofSeconds(0);
    protected Duration leaseTime = ChronoUnit.FOREVER.getDuration();
    protected Runnable runnable;
    protected Supplier<?> supplier;

    private String getDefaultLockKey() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[5];
        return stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
    }

    protected long convertToNanos(Duration duration) {
        return TimeUnit.SECONDS.toNanos(duration.getSeconds()) + duration.getNano();
    }

    protected abstract void executeRunnable() throws LockedException;

    protected abstract <T> T executeSupplier() throws LockedException;

    public enum LockType {
        LOCK, TRY_LOCK
    }

    public class LockKeyStep extends LockTypeStep {
        public LockTypeStep lockKey(Object lockKey) {
            AbstractLockBuilder.this.lockKey = lockKey;
            return new LockTypeStep();
        }
    }

    public class LockTypeStep {
        public TaskStep lock() {
            AbstractLockBuilder.this.lockType = LockType.LOCK;
            return new TaskStep();
        }

        public TaskStep tryLock() {
            AbstractLockBuilder.this.lockType = LockType.TRY_LOCK;
            return new TaskStep();
        }

        public TaskStep tryLock(Duration waitTime) {
            AbstractLockBuilder.this.waitTime = waitTime;
            return tryLock();
        }

        public TaskStep tryLock(Duration waitTime, Duration leaseTime) {
            AbstractLockBuilder.this.leaseTime = leaseTime;
            return tryLock(waitTime);
        }
    }

    public class TaskStep {
        public RunnableStep task(Runnable runnable) {
            AbstractLockBuilder.this.runnable = runnable;
            return new RunnableStep();
        }

        public <T> SupplierStep<T> task(Supplier<T> supplier) {
            AbstractLockBuilder.this.supplier = supplier;
            return new SupplierStep<>();
        }
    }

    public class RunnableStep {
        public void execute() throws LockedException {
            executeRunnable();
        }
    }

    public class SupplierStep<T> {
        public T execute() throws LockedException {
            return executeSupplier();
        }
    }
}
