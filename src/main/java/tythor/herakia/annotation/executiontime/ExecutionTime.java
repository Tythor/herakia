package tythor.herakia.annotation.executiontime;

import org.slf4j.event.Level;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExecutionTime {
    String signature() default "";
    String tag() default "";
    Level level() default Level.INFO;
    long logFrequency() default 1;
    // Flag to reset ExecutionStats after logging
    boolean reset() default true;
    boolean disableLog() default false;
}
