package tythor.herakia.utility.lock;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tythor.herakia.exception.LockedException;
import tythor.herakia.utility.SignatureUtil;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractLockBuilder {
    protected Object lockKey = SignatureUtil.getSignature(4);
    protected LockType lockType;
    protected boolean throwUnchecked;
    protected Duration waitDuration = Duration.ofSeconds(0);
    protected Duration leaseDuration = ChronoUnit.FOREVER.getDuration();
    protected Duration sleepDuration = Duration.ofSeconds(0);
    protected Runnable runnable;
    protected Supplier<?> supplier;

    protected long convertToNanos(Duration duration) {
        return TimeUnit.SECONDS.toNanos(duration.getSeconds()) + duration.getNano();
    }

    protected void sleep() {
        try {
            if (sleepDuration.isPositive()) {
                log.info("Sleeping for {}...", sleepDuration);
                Thread.sleep(sleepDuration);
            }
        } catch (Exception e) {}
    }

    protected abstract void executeRunnable();

    protected abstract <T> T executeSupplier();

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
        public SleepOrTaskStep lock() {
            AbstractLockBuilder.this.lockType = LockType.LOCK;
            return new SleepOrTaskStep();
        }

        public SleepOrTaskStep tryLock() throws LockedException {
            AbstractLockBuilder.this.lockType = LockType.TRY_LOCK;
            return new SleepOrTaskStep();
        }

        public SleepOrTaskStep tryLock(Duration waitTime) throws LockedException {
            AbstractLockBuilder.this.waitDuration = waitTime;
            return tryLock();
        }

        public SleepOrTaskStep tryLock(Duration waitTime, Duration leaseTime) throws LockedException {
            AbstractLockBuilder.this.leaseDuration = leaseTime;
            return tryLock(waitTime);
        }

        @SneakyThrows
        public SleepOrTaskStep tryLockUnchecked() {
            AbstractLockBuilder.this.throwUnchecked = true;
            return tryLock();
        }

        public SleepOrTaskStep tryLockUnchecked(Duration waitTime) {
            AbstractLockBuilder.this.waitDuration = waitTime;
            return tryLockUnchecked();
        }

        public SleepOrTaskStep tryLockUnchecked(Duration waitTime, Duration leaseTime) {
            AbstractLockBuilder.this.leaseDuration = leaseTime;
            return tryLockUnchecked(waitTime);
        }
    }

    public class SleepOrTaskStep extends TaskStep {
        public TaskStep sleep(Duration sleepTime) {
            AbstractLockBuilder.this.sleepDuration = sleepTime;
            return new TaskStep();
        }
    }

    public class TaskStep {
        public RunnableStep task(Runnable runnable) {
            AbstractLockBuilder.this.runnable = () -> {
                sleep();
                runnable.run();
            };
            return new RunnableStep();
        }

        public <T> SupplierStep<T> task(Supplier<T> supplier) {
            AbstractLockBuilder.this.supplier = () -> {
                sleep();
                return supplier.get();
            };
            return new SupplierStep<>();
        }
    }

    public class RunnableStep {
        public void execute() {
            executeRunnable();
        }
    }

    public class SupplierStep<T> {
        public T execute() {
            return executeSupplier();
        }
    }
}
