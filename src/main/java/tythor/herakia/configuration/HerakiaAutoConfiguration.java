package tythor.herakia.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tythor.herakia.hazelcast.management.HazelcastManagementConfig;

@Configuration
@AutoConfiguration
@Import({
    ExecutorConfiguration.class,
    JacksonConfiguration.class,
    HerakiaCoreConfiguration.class,
    HazelcastManagementConfig.class
})
public class HerakiaAutoConfiguration {}
