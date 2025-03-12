package tythor.herakia.hazelcast.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tythor.herakia.hazelcast.management.component.*;

@Configuration
@RequiredArgsConstructor
public class HazelcastManagementCoreConfig {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Bean
    @ConditionalOnMissingBean
    public HazelcastStatsCollector hazelcastStatsCollector() {
        return new HazelcastStatsCollector(hzMapService());
    }

    @Bean
    @ConditionalOnMissingBean
    public HzController hzController() {
        return new HzController(hazelcastStatsCollector());
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

    @Bean
    @ConditionalOnMissingBean
    public MemberSerializer memberSerializer() {
        return new MemberSerializer();
    }
}
