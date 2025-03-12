package tythor.herakia.utility;

import lombok.extern.slf4j.Slf4j;
import tythor.herakia.annotation.executiontime.ExecutionTimeConfig;
import tythor.herakia.annotation.executiontime.ExecutionTimeLogger;
import tythor.herakia.utility.thread.ThreadStorage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
public class ExecutionTimeUtil {
    public static void logExecutionTime(Runnable runnable) {
        logExecutionTime(runnable, ExecutionTimeUtil.getExecutionTimeConfig(SignatureUtil.getSimpleSignature(2)));
    }

    public static void logExecutionTime(Runnable runnable, ExecutionTimeConfig executionTimeConfig) {
        setExecutionTimeConfig(executionTimeConfig);
        ExecutionTimeLogger executionTimeLogger = SpringUtil.getBean(ExecutionTimeLogger.class);
        executionTimeLogger.logExecutionTime(runnable, executionTimeConfig.getSignature());
    }


    public static <T> T logExecutionTime(Supplier<T> supplier) {
        return logExecutionTime(supplier, ExecutionTimeUtil.getExecutionTimeConfig(SignatureUtil.getSimpleSignature(2)));
    }

    public static <T> T logExecutionTime(Supplier<T> supplier, ExecutionTimeConfig executionTimeConfig) {
        setExecutionTimeConfig(executionTimeConfig);
        ExecutionTimeLogger executionTimeLogger = SpringUtil.getBean(ExecutionTimeLogger.class);
        return executionTimeLogger.logExecutionTime(supplier, executionTimeConfig.getSignature());
    }


    public static ExecutionTimeConfig getExecutionTimeConfig() {
        return getExecutionTimeConfig(SignatureUtil.getSimpleSignature());
    }

    public static ExecutionTimeConfig getExecutionTimeConfig(String signature) {
        Map<String, ExecutionTimeConfig> executionTimeConfigMap = getExecutionTimeConfigMap();
        return executionTimeConfigMap.computeIfAbsent(signature, ExecutionTimeConfig::new);
    }

    private static void setExecutionTimeConfig(ExecutionTimeConfig executionTimeConfig) {
        Map<String, ExecutionTimeConfig> executionTimeConfigMap = getExecutionTimeConfigMap();
        executionTimeConfigMap.put(executionTimeConfig.getSignature(), executionTimeConfig);
    }

    private static Map<String, ExecutionTimeConfig> getExecutionTimeConfigMap() {
        Map<String, ExecutionTimeConfig> map = ThreadStorage.get(ExecutionTimeConfig.KEY);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            ThreadStorage.put(ExecutionTimeConfig.KEY, map);
        }
        return map;
    }
}
