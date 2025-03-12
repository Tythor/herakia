package tythor.herakia.annotation.executiontime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;
import tythor.herakia.utility.DigitUtil;
import tythor.herakia.utility.ExecutionTimeUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@Component
public class ExecutionTimeLogger {
    // Group ExecutionStats by signature
    private final Map<String, AtomicReference<ExecutionStats>> executionStatsMap = new ConcurrentHashMap<>();

    public void logExecutionTime(Runnable runnable, String signature) {
        long startTime = System.nanoTime();

        try {
            runnable.run();
        } finally {
            logExecutionTime(startTime, signature);
        }
    }

    public <T> T logExecutionTime(Supplier<T> supplier, String signature) {
        long startTime = System.nanoTime();

        try {
            return supplier.get();
        } finally {
            logExecutionTime(startTime, signature);
        }
    }

    private void logExecutionTime(long startTime, String signature) {
        ExecutionTimeConfig executionTimeConfig = ExecutionTimeUtil.getExecutionTimeConfig(signature);
        if (executionTimeConfig.getDisableLog() == Boolean.TRUE) return;

        double elapsedTime = System.nanoTime() - startTime;

        // Create or retrieve the AtomicReference for the current signature
        AtomicReference<ExecutionStats> executionStatsRef = executionStatsMap.computeIfAbsent(executionTimeConfig.getSignature(), k -> new AtomicReference<>(new ExecutionStats()));

        // Update the AtomicReference for the current signature
        ExecutionStats executionStats = executionStatsRef.updateAndGet(currentStats ->
            new ExecutionStats(currentStats.getExecutionCount() + 1, currentStats.getTotalTime() + elapsedTime));

        // Log the average execution time according to the logFrequency
        if (executionStats.getExecutionCount() % executionTimeConfig.getLogFrequency() == 0) {
            String logLine = "[%s]".formatted(signature);

            if (!executionTimeConfig.getTag().isBlank()) logLine += " - [%s]".formatted(executionTimeConfig.getTag());

            double avgExecutionTime = executionStats.getTotalTime() / executionStats.getExecutionCount() / 1_000_000;
            logLine += ": %sms".formatted(DigitUtil.truncateDouble(avgExecutionTime, 3));

            log.atLevel(executionTimeConfig.getLevel()).log(logLine);

            if (executionTimeConfig.getReset()) executionStatsRef.set(new ExecutionStats());
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
