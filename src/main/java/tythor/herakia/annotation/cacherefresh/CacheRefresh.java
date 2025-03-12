package tythor.herakia.annotation.cacherefresh;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to refresh the values in a {@link org.springframework.cache.Cache Cache} according to the cron expression.
 * <p>
 * Must be used with {@link org.springframework.cache.annotation.Cacheable @Cacheable}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheRefresh {
    /**
     * @see org.springframework.cache.annotation.Cacheable#value()
     */
    String[] value();

    /**
     * @see org.springframework.scheduling.annotation.Scheduled#cron()
     */
    String cron();

    /**
     * @see org.springframework.scheduling.annotation.Scheduled#zone()
     */
    String zone() default "";

    /**
     * @see org.springframework.context.annotation.Profile#value()
     */
    String[] profiles() default {};
}
