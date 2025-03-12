package tythor.herakia.hazelcast.management.util;

import com.hazelcast.map.IMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MapClassCacheUtil {
    private static final Map<String, MapClass> mapClassCache = new HashMap<>();

    public static MapClass getMapClass(IMap<?, ?> map) {
        return mapClassCache.computeIfAbsent(map.getName(), key -> {
            if (!map.isEmpty()) {
                try {
                    Set<?> localKeySet = map.localKeySet();
                    Object sampleKey = !localKeySet.isEmpty() ? localKeySet.iterator().next() : map.keySet().iterator().next();
                    Object sampleValue = map.get(sampleKey);
                    return new MapClass(sampleKey.getClass(), sampleValue.getClass());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    @Data
    @AllArgsConstructor
    public static class MapClass {
        private Class<?> keyClass;
        private Class<?> valueClass;
    }
}
