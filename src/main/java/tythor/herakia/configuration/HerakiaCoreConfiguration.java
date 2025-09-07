package tythor.herakia.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import tythor.herakia.annotation.cacherefresh.CacheRefreshProcessor;
import tythor.herakia.annotation.executiontime.ExecutionTimeAspect;
import tythor.herakia.annotation.executiontime.ExecutionTimeProcessor;
import tythor.herakia.component.*;

@Configuration
public class HerakiaCoreConfiguration {
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnMissingBean
    public AdvisoryLockManager advisoryLockManager(HikariDataSource hikariDataSource) {
        return new AdvisoryLockManager(hikariDataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public AspectWrapperUtil aspectBridgeUtil() {
        return new AspectWrapperUtil();
    }

    @Bean
    @ConditionalOnMissingBean
    public BootstrapCoordinator bootstrapCoordinator(ObjectProvider<AbstractBootstrapService> bootstrapServices) {
        return new BootstrapCoordinator(bootstrapServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheRefreshProcessor cacheRefreshProcessor(CacheManager cacheManager, TaskScheduler taskScheduler, Environment environment) {
        return new CacheRefreshProcessor(cacheManager, taskScheduler, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutionTimeAspect executionTimeAspect() {
        return new ExecutionTimeAspect(executionTimeProcessor());
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutionTimeProcessor executionTimeProcessor() {
        return new ExecutionTimeProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebServerConfig webServerConfig() {
        return new WebServerConfig();
    }
}
