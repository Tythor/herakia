package tythor.herakia.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tythor.herakia.annotation.executiontime.ExecutionTimeAspect;
import tythor.herakia.annotation.executiontime.ExecutionTimeProcessor;
import tythor.herakia.component.AdvisoryLockManager;
import tythor.herakia.component.AspectWrapperUtil;

@Configuration
@AutoConfiguration
public class HerakiaAutoConfiguration {
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
