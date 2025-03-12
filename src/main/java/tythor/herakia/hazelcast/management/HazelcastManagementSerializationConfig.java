package tythor.herakia.hazelcast.management;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tythor.herakia.hazelcast.management.component.MemberSerializer;
import tythor.herakia.hazelcast.management.component.SimpleKeyDeserializer;

@Configuration
public class HazelcastManagementSerializationConfig {
    @Bean
    @ConditionalOnMissingBean
    public MemberSerializer memberSerializer() {
        return new MemberSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleKeyDeserializer simpleKeyDeserializer() {
        return new SimpleKeyDeserializer();
    }
}
