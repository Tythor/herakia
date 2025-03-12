package tythor.herakia.utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.SneakyThrows;

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
        String deserializedString = objectMapper.copy()
            .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                @Override
                public boolean hasIgnoreMarker(final AnnotatedMember m) {
                    return Arrays.asList(fields).contains(m.getName()) || super.hasIgnoreMarker(m);
                }})
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
