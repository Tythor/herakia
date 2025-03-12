package tythor.herakia.annotation.executiontime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@Component
public class ExecutionTimeProcessor {
    // Group ExecutionStats by signature
    private final Map<String, AtomicReference<ExecutionStats>> executionStatsMap = new ConcurrentHashMap<>();

    public void logExecutionTime(Runnable runnable, String signature, long logFrequency, boolean reset) {
        long startTime = System.nanoTime();

        try {
            runnable.run();
        } finally {
            logExecutionTime(signature, logFrequency, reset, startTime);
        }
    }

    public <T> T logExecutionTime(Supplier<T> supplier, String signature, long logFrequency, boolean reset) {
        long startTime = System.nanoTime();

        try {
            return supplier.get();
        } finally {
            logExecutionTime(signature, logFrequency, reset, startTime);
        }
    }

    private void logExecutionTime(String signature, long logFrequency, boolean reset, long startTime) {
        double elapsedTime = System.nanoTime() - startTime;

        // Create or retrieve the AtomicReference for the current signature
        AtomicReference<ExecutionStats> executionStatsRef = executionStatsMap.computeIfAbsent(signature, k -> new AtomicReference<>(new ExecutionStats()));

        // Update the AtomicReference for the current signature
        ExecutionStats executionStats = executionStatsRef.updateAndGet(currentStats ->
            new ExecutionStats(currentStats.getExecutionCount() + 1, currentStats.getTotalTime() + elapsedTime));

        // Log the average execution time according to the logFrequency
        if (executionStats.getExecutionCount() % logFrequency == 0) {
            String averageExecutionTime = String.format("%.2f", executionStats.getTotalTime() / executionStats.getExecutionCount() / 1_000_000);
            log.info("[signature={}] - Avg Execution Time: {}ms", signature, averageExecutionTime);
            if (reset) executionStatsRef.set(new ExecutionStats());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExecutionStats {
        private long executionCount;
        private double totalTime;
    }
}
