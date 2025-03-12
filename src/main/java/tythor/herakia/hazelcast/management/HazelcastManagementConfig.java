package tythor.herakia.hazelcast.management;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    HazelcastManagementSerializationConfig.class,
    HazelcastManagementCoreConfig.class
})
public class HazelcastManagementConfig {}
