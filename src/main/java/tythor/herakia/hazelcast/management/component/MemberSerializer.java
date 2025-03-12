package tythor.herakia.hazelcast.management.component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hazelcast.cluster.Member;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class MemberSerializer extends JsonSerializer<Member> {
    @Override
    public void serialize(Member member, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(member);
        gen.writeTree(jsonNode);
    }
}
