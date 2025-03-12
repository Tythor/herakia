package tythor.herakia.hazelcast.management;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tythor.herakia.hazelcast.management.component.MemberSerializer;

@Configuration
public class HazelcastManagementSerializationConfig {
    @Bean
    @ConditionalOnMissingBean
    public MemberSerializer memberSerializer() {
        return new MemberSerializer();
    }
}
