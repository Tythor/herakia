package tythor.herakia.hazelcast.management;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tythor.herakia.hazelcast.management.component.MemberSerializer;
import tythor.herakia.hazelcast.management.component.SimpleKeyDeserializer;

@Configuration
@Import({
    MemberSerializer.class,
    SimpleKeyDeserializer.class
})
public class HazelcastManagementSerializationConfig {}
