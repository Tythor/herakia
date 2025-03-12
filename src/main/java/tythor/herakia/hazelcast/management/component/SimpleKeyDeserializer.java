package tythor.herakia.hazelcast.management.component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.cache.interceptor.SimpleKey;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@JsonComponent
public class SimpleKeyDeserializer extends JsonDeserializer<SimpleKey> {
    @SuppressWarnings("unchecked")
    @Override
    public SimpleKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, List<?>> map = ctxt.readValue(p, Map.class);
        Object[] params = map.get("params").toArray();

        if (params.length == 0) {
            return SimpleKey.EMPTY;
        } else {
            return new SimpleKey(params);
        }
    }
}
