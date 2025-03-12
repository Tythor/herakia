package tythor.herakia.utility;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tythor.herakia.annotation.executiontime.ExecutionTime;
import tythor.herakia.annotation.executiontime.ExecutionTimeProcessor;

import java.util.function.Supplier;

@Slf4j
public class ExecutionTimeUtil {
    public static void logExecutionTime(Runnable runnable) {
        String defaultSignature = SignatureUtil.getSimpleSignature();
        logExecutionTime(runnable, defaultSignature);
    }

    @SneakyThrows
    public static void logExecutionTime(Runnable runnable, String signature) {
        long logFrequency = (Long) ExecutionTime.class.getMethod("logFrequency").getDefaultValue();
        logExecutionTime(runnable, signature, logFrequency);
    }

    @SneakyThrows
    public static void logExecutionTime(Runnable runnable, String signature, long logFrequency) {
        boolean reset = (Boolean) ExecutionTime.class.getMethod("reset").getDefaultValue();
        logExecutionTime(runnable, signature, logFrequency, reset);
    }

    public static void logExecutionTime(Runnable runnable, String signature, long logFrequency, boolean reset) {
        ExecutionTimeProcessor executionTimeProcessor = SpringUtil.getBean(ExecutionTimeProcessor.class);
        executionTimeProcessor.logExecutionTime(runnable, signature, logFrequency, reset);
    }


    public static <T> T logExecutionTime(Supplier<T> supplier) {
        String defaultSignature = SignatureUtil.getSimpleSignature();
        return logExecutionTime(supplier, defaultSignature);
    }

    @SneakyThrows
    public static <T> T logExecutionTime(Supplier<T> supplier, String signature) {
        long logFrequency = (Long) ExecutionTime.class.getMethod("logFrequency").getDefaultValue();
        return logExecutionTime(supplier, signature, logFrequency);
    }

    @SneakyThrows
    public static <T> T logExecutionTime(Supplier<T> supplier, String signature, long logFrequency) {
        boolean reset = (Boolean) ExecutionTime.class.getMethod("reset").getDefaultValue();
        return logExecutionTime(supplier, signature, logFrequency, reset);
    }

    public static <T> T logExecutionTime(Supplier<T> supplier, String signature, long logFrequency, boolean reset) {
        ExecutionTimeProcessor executionTimeProcessor = SpringUtil.getBean(ExecutionTimeProcessor.class);
        return executionTimeProcessor.logExecutionTime(supplier, signature, logFrequency, reset);
    }
}
