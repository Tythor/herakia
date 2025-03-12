package tythor.herakia.hazelcast.management.component;

import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.boot.jackson.ObjectValueDeserializer;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@JacksonComponent
public class SimpleKeyDeserializer extends ObjectValueDeserializer<SimpleKey> {
    @SuppressWarnings("unchecked")
    @Override
    protected SimpleKey deserializeObject(JsonParser jsonParser, DeserializationContext context, JsonNode tree) {
        Map<String, List<?>> map = context.readTreeAsValue(tree, Map.class);
        List<?> params = map.get("params");
        return CollectionUtils.isEmpty(params) ? SimpleKey.EMPTY : new SimpleKey(params.toArray());
    }
}
