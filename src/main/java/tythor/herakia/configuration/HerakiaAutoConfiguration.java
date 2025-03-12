package tythor.herakia.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tythor.herakia.annotation.ExecutionTimeAspect;
import tythor.herakia.utility.AdvisoryLockManager;
import tythor.herakia.utility.AspectBridgeUtil;

import javax.sql.DataSource;

@Configuration
@AutoConfiguration
public class HerakiaAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AdvisoryLockManager advisoryLockManager(DataSource dataSource) {
        return new AdvisoryLockManager(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public AspectBridgeUtil aspectBridgeUtil() {
        return new AspectBridgeUtil();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutionTimeAspect executionTimeAspect() {
        return new ExecutionTimeAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebServerConfig webServerConfig() {
        return new WebServerConfig();
    }
}
