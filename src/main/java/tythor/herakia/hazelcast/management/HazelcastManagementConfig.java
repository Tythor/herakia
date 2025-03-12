package tythor.herakia.hazelcast.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tythor.herakia.hazelcast.management.component.*;

@Configuration
@AutoConfiguration
@RequiredArgsConstructor
public class HazelcastManagementConfig {
    private final ObjectMapper objectMapper;
    @Qualifier("nonReflectionObjectMapper")
    private final ObjectMapper nonReflectionObjectMapper;
    private final RestTemplate restTemplate;

    @Bean
    @ConditionalOnMissingBean
    public HazelcastStatsCollector hazelcastStatsCollector() {
        return new HazelcastStatsCollector(hzMapService());
    }

    @Bean
    @ConditionalOnMissingBean
    public HzController hzController() {
        return new HzController(nonReflectionObjectMapper, hazelcastStatsCollector());
    }

    @Bean
    @ConditionalOnMissingBean
    public HzKeyResolver hzKeyResolver() {
        return new HzKeyResolver(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HzMapController hzMapController() {
        return new HzMapController(hazelcastStatsCollector(), hzMapService());
    }

    @Bean
    @ConditionalOnMissingBean
    public HzMapService hzMapService() {
        return new HzMapService(hzKeyResolver(), objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HzReverseProxy hzReverseProxy() {
        return new HzReverseProxy(restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public HzHealthIndicator hzHealthIndicator() {
        return new HzHealthIndicator(restTemplate);
    }
}
