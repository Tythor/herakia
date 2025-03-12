package tythor.herakia.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import tythor.herakia.annotation.cacherefresh.CacheRefreshProcessor;
import tythor.herakia.annotation.executiontime.ExecutionTimeAspect;
import tythor.herakia.annotation.executiontime.ExecutionTimeProcessor;
import tythor.herakia.component.*;
import tythor.herakia.hazelcast.management.HazelcastManagementCoreConfig;
import tythor.herakia.hazelcast.management.HazelcastManagementSerializationConfig;

@Configuration
@Import({
    AspectWrapperUtil.class,
    BootstrapCoordinator.class,
    CacheRefreshProcessor.class,
    ExecutionTimeAspect.class,
    ExecutionTimeProcessor.class,
    WebServerConfig.class
})
public class HerakiaCoreConfiguration {
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnMissingBean
    @ConditionalOnBean(HikariDataSource.class)
    public AdvisoryLockManager advisoryLockManager(HikariDataSource hikariDataSource) {
        return new AdvisoryLockManager(hikariDataSource);
    }
}
