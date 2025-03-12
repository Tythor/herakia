package tythor.herakia.utility.thread;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import tythor.herakia.utility.LoggingUtil;
import tythor.herakia.utility.SignatureUtil;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
public class VirtualThreadUtil {
    private static final Map<String, ThreadFactory> VIRTUAL_FACTORY_MAP = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> VIRTUAL_EXECUTOR_MAP = new ConcurrentHashMap<>();

    public static ThreadFactory getVirtualFactory(String prefix) {
        Function<String, ThreadFactory> virtualFactoryFunction = key -> Thread.ofVirtual().name(prefix + "-", 0).factory();
        return VIRTUAL_FACTORY_MAP.computeIfAbsent(prefix, virtualFactoryFunction);
    }

    public static ExecutorService getVirtualExecutor(String prefix) {
        return VIRTUAL_EXECUTOR_MAP.computeIfAbsent(prefix, VirtualThreadUtil::getNewVirtualExecutor);
    }

    /**
     * Used for try-with-resources executors: {@code try (ExecutorService executorService = VirtualThreadUtil.getNewVirtualExecutor())}
     */
    public static ExecutorService getNewVirtualExecutor(String prefix) {
        ExecutorService executorService = Executors.newThreadPerTaskExecutor(getVirtualFactory(prefix));
        Runtime.getRuntime().addShutdownHook(getVirtualFactory().newThread(() -> {
            if (!executorService.isTerminated()) {
                LoggingUtil.logIfAvailable(Level.INFO, "ExecutorService=%s - Starting shutdown...".formatted(prefix));

                ScheduledExecutorService watchdogExecutor = Executors.newSingleThreadScheduledExecutor(getVirtualFactory(prefix));
                long startTime = System.currentTimeMillis();
                Runnable logTask = () -> LoggingUtil.logIfAvailable(Level.INFO, "ExecutorService=%s - Shutting down for %ss...".formatted(prefix, (startTime - System.currentTimeMillis()) / 1000));
                watchdogExecutor.scheduleAtFixedRate(logTask, 30, 30, TimeUnit.SECONDS);

                executorService.close();
                watchdogExecutor.close();

                LoggingUtil.logIfAvailable(Level.INFO, "ExecutorService=%s - Completed shutdown.".formatted(prefix));
            }
        }));
        return executorService;
    }

    public static void runAsVirtual(String prefix, Runnable task) {
        getVirtualFactory(prefix).newThread(task).start();
    }

    public static void executeAsVirtual(String prefix, Runnable task) {
        getVirtualExecutor(prefix).execute(task);
    }

    // -------------------- HELPER METHODS --------------------

    public static ThreadFactory getVirtualFactory() {
        String simpleClassName = SignatureUtil.getSimpleClassName(2);
        return getVirtualFactory(simpleClassName);
    }

    public static ExecutorService getVirtualExecutor() {
        String simpleClassName = SignatureUtil.getSimpleClassName(2);
        return getVirtualExecutor(simpleClassName);
    }

    public static ExecutorService getNewVirtualExecutor() {
        String simpleClassName = SignatureUtil.getSimpleClassName(2);
        return getNewVirtualExecutor(simpleClassName);
    }

    public static void runAsVirtual(Runnable task) {
        String simpleClassName = SignatureUtil.getSimpleClassName(2);
        runAsVirtual(simpleClassName, task);
    }

    public static void executeAsVirtual(Runnable task) {
        String simpleClassName = SignatureUtil.getSimpleClassName(2);
        executeAsVirtual(simpleClassName, task);
    }
}
