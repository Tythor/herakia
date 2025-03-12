package tythor.herakia.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tythor.herakia.annotation.cacherefresh.CacheRefreshProcessor;
import tythor.herakia.annotation.executiontime.ExecutionTimeAspect;
import tythor.herakia.annotation.executiontime.ExecutionTimeLogger;
import tythor.herakia.component.*;

@Configuration
@Import({
    AspectWrapperUtil.class,
    BootstrapOrchestrator.class,
    CacheRefreshProcessor.class,
    ExecutionTimeAspect.class,
    ExecutionTimeLogger.class,
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
