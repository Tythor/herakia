package tythor.herakia.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tythor.herakia.hazelcast.management.HazelcastManagementConfig;

@Configuration
@AutoConfiguration
@AutoConfigureBefore({
    TaskExecutionAutoConfiguration.class,
    TaskSchedulingAutoConfiguration.class,
    JacksonAutoConfiguration.class
})
@Import({
    ExecutorConfiguration.class,
    WebClientConfiguration.class,
    JacksonConfiguration.class,
    HerakiaCoreConfiguration.class,
    HazelcastManagementConfig.class
})
public class HerakiaAutoConfiguration {}
