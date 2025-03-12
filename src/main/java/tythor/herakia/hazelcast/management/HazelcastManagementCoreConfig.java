package tythor.herakia.hazelcast.management;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tythor.herakia.hazelcast.management.component.*;

@Configuration
@Import({
    HazelcastStatsCollector.class,
    HzController.class,
    HzKeyResolver.class,
    HzMapController.class,
    HzMapService.class,
    HzReverseProxy.class,
    HzHealthIndicator.class,
    MemberSerializer.class
})
public class HazelcastManagementCoreConfig {}
