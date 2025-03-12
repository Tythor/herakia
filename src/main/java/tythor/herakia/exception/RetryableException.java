package tythor.herakia.exception;

import lombok.experimental.StandardException;

/**
 * A wrapper exception to be used with {@link org.springframework.resilience.annotation.Retryable @Retryable}.
 */
@StandardException
public class RetryableException extends RuntimeException {}
