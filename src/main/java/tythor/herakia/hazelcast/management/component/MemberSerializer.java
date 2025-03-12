package tythor.herakia.hazelcast.management.component;

import com.hazelcast.cluster.Member;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.boot.jackson.ObjectValueSerializer;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;

@JacksonComponent
public class MemberSerializer extends ObjectValueSerializer<Member> {
    @Override
    protected void serializeObject(Member value, JsonGenerator jgen, SerializationContext context) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(value);
        jgen.writeRaw(json.substring(1, json.length() - 1));
    }
}
