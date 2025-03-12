package tythor.herakia.annotation.executiontime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExecutionTimeAspect {
    private final ExecutionTimeProcessor executionTimeProcessor;

    @Around("@annotation(tythor.herakia.annotation.executiontime.ExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Supplier<Object> aspectSupplier = () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };

        // Get or initialize the AtomicReference for the current signature
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        String className = methodSignature.getDeclaringTypeName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = methodSignature.getName();
        String signature = simpleClassName + "." + methodName;

        ExecutionTime executionTime = methodSignature.getMethod().getAnnotation(ExecutionTime.class);

        return executionTimeProcessor.logExecutionTime(aspectSupplier, signature, executionTime.logFrequency(), executionTime.reset());
    }
}
