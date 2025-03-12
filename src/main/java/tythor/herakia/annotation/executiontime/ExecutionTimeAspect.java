package tythor.herakia.annotation.executiontime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {
    // Group ExecutionStats by signature
    private final ConcurrentHashMap<String, AtomicReference<ExecutionStats>> executionStatsMap = new ConcurrentHashMap<>();

    @Around("@annotation(tythor.herakia.annotation.executiontime.ExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            double queryTime = System.nanoTime() - startTime;

            // Get or initialize the AtomicReference for the current signature
            MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
            String className = methodSignature.getDeclaringTypeName();
            String methodName = methodSignature.getName();
            String signature = className + "." + methodName;
            AtomicReference<ExecutionStats> executionStatsRef = executionStatsMap.computeIfAbsent(signature, k -> new AtomicReference<>(new ExecutionStats()));

            // Update the AtomicReference for the current signature
            ExecutionStats executionStats = executionStatsRef.updateAndGet(currentStats ->
                new ExecutionStats(currentStats.getExecutionCount() + 1, currentStats.getTotalTime() + queryTime));

            // Log the average execution time according to the logFrequency
            ExecutionTime executionTime = methodSignature.getMethod().getAnnotation(ExecutionTime.class);
            if (executionStats.getExecutionCount() % executionTime.logFrequency() == 0) {
                String averageExecutionTime = String.format("%.2f", executionStats.getTotalTime() / executionStats.getExecutionCount() / 1_000_000);
                log.info("[method={}] - Avg Execution Time: {}ms", methodName, averageExecutionTime);
                if (executionTime.reset()) executionStatsRef.set(new ExecutionStats());
            }
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
