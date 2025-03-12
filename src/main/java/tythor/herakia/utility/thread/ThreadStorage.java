package tythor.herakia.utility.thread;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadStorage {
    private static final InheritableThreadLocal<Map<Object, Object>> MAP_THREAD_LOCAL = ThreadLocalUtil.withInitial(ConcurrentHashMap::new);

    public static <T> Optional<T> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) MAP_THREAD_LOCAL.get().get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T put(String key, T value) {
        return (T) MAP_THREAD_LOCAL.get().put(key, value);
    }
}
