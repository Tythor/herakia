package tythor.herakia.utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

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

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T cloneUsingJackson(T object) {
        String deserializedString = objectMapper.writeValueAsString(object);
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
