package tythor.herakia.utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import lombok.SneakyThrows;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.util.Arrays;

public class CloneUtil {
    private static final ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
    private static final Pool<Kryo> kryoPool = new Pool<>(true, true) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    };

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T cloneUsingJackson(T object) {
        String deserializedString = objectMapper.writeValueAsString(object);
        return (T) objectMapper.readValue(deserializedString, object.getClass());
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T cloneUsingJacksonIgnoring(T object, String... fields) {
        String deserializedString = SpringUtil.getBean(JsonMapper.Builder.class)
            .annotationIntrospector(new JacksonAnnotationIntrospector() {
                @Override
                public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
                    return Arrays.asList(fields).contains(m.getName()) || super.hasIgnoreMarker(config, m);
                }})
            .build()
            .writer(new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(fields)))
            .writeValueAsString(object);

        return (T) objectMapper.readValue(deserializedString, object.getClass());
    }

    public static <T> T cloneUsingKryo(T object) {
        Kryo kryo = null;
        try {
            kryo = kryoPool.obtain();
            return kryo.copy(object);
        } finally {
            if (kryo != null) {
                kryoPool.free(kryo);
            }
        }
    }
}
