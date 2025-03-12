package tythor.herakia.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

public class VirtualThreadUtil {
    private static final Map<String, ThreadFactory> VIRTUAL_FACTORY_MAP = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> VIRTUAL_EXECUTOR_MAP = new ConcurrentHashMap<>();

    public static ThreadFactory getVirtualFactory(String prefix) {
        Function<String, ThreadFactory> virtualFactoryFunction = key -> Thread.ofVirtual().name(prefix + "-", 0).factory();
        return VIRTUAL_FACTORY_MAP.computeIfAbsent(prefix, virtualFactoryFunction);
    }

    public static ExecutorService getVirtualExecutor(String prefix) {
        Function<String, ExecutorService> virtualExecutorFunction = key -> Executors.newThreadPerTaskExecutor(getVirtualFactory(prefix));
        return VIRTUAL_EXECUTOR_MAP.computeIfAbsent(prefix, virtualExecutorFunction);
    }

    /**
     * Used for try-with-resources executors: {@code try (ExecutorService executorService = VirtualThreadUtil.getNewVirtualExecutor())}
     */
    public static ExecutorService getNewVirtualExecutor(String prefix) {
        return Executors.newThreadPerTaskExecutor(getVirtualFactory(prefix));
    }

    public static void runAsVirtual(String prefix, Runnable task) {
        getVirtualFactory(prefix).newThread(task).start();
    }

    public static void executeAsVirtual(String prefix, Runnable task) {
        getVirtualExecutor(prefix).execute(task);
    }

    // -------------------- HELPER METHODS --------------------

    public static ThreadFactory getVirtualFactory() {
        String simpleClassName = getSimpleClassName();
        return getVirtualFactory(simpleClassName);
    }

    public static ExecutorService getVirtualExecutor() {
        String simpleClassName = getSimpleClassName();
        return getVirtualExecutor(simpleClassName);
    }

    public static ExecutorService getNewVirtualExecutor() {
        String simpleClassName = getSimpleClassName();
        return getNewVirtualExecutor(simpleClassName);
    }

    public static void runAsVirtual(Runnable task) {
        String simpleClassName = getSimpleClassName();
        runAsVirtual(simpleClassName, task);
    }

    public static void executeAsVirtual(Runnable task) {
        String simpleClassName = getSimpleClassName();
        executeAsVirtual(simpleClassName, task);
    }

    private static String getSimpleClassName() {
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
