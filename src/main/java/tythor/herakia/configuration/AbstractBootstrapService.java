package tythor.herakia.configuration;

/**
 * An abstraction for startup behavior.
 * <p>
 * <b>Note:</b> When registering multiple implementations, use {@link org.springframework.core.annotation.Order} to define the bootstrap order.
 * @see tythor.herakia.component.BootstrapOrchestrator
 */
public interface AbstractBootstrapService {
    void preStartup();
    void postStartup();
}
