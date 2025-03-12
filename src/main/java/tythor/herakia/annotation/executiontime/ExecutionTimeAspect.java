package tythor.herakia.annotation.executiontime;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import tythor.herakia.utility.ExecutionTimeUtil;

import java.util.function.Supplier;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExecutionTimeAspect {
    private final ExecutionTimeLogger executionTimeLogger;

    @Around("@annotation(tythor.herakia.annotation.executiontime.ExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        Supplier<?> aspectSupplier = () -> proceedJoinPoint(joinPoint);

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        String className = methodSignature.getDeclaringTypeName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = methodSignature.getName();
        String signature = simpleClassName + "." + methodName;

        ExecutionTime executionTime = methodSignature.getMethod().getAnnotation(ExecutionTime.class);
        if (executionTime == null) {
            executionTime = joinPoint.getTarget().getClass()
                .getMethod(methodSignature.getMethod().getName(), methodSignature.getMethod().getParameterTypes())
                .getAnnotation(ExecutionTime.class);
        }

        signature = executionTime.signature().isBlank() ? signature : executionTime.signature();
        ExecutionTimeUtil.getExecutionTimeConfig(signature)
            .setTag(executionTime.tag())
            .setLogFrequency(executionTime.logFrequency())
            .setReset(executionTime.reset())
            .setDisableLog(executionTime.disableLog());

        return executionTimeLogger.logExecutionTime(aspectSupplier, signature);
    }

    @SneakyThrows
    private static Object proceedJoinPoint(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }
}
