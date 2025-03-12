package tythor.herakia.hazelcast.management.callable;

import tythor.herakia.hazelcast.management.dto.MapStats;
import com.hazelcast.map.IMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tythor.herakia.utility.HazelcastUtil;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Slf4j
@AllArgsConstructor
public class ObjectStatsCollector<T> implements Callable<T>, Serializable {
    private final Class<T> type;
    private final String name;
    private final boolean showConfig;

    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        if (type.isAssignableFrom(MapStats.class)) {
            return (T) createMapStats();
        } else {
            log.debug("Skipping unsupported stat type: {}", type);
            return null;
        }
    }

    private MapStats createMapStats() {
        IMap<?, ?> map = HazelcastUtil.getMap(name);
        int totalBackups = HazelcastUtil.getInstance().getConfig().getMapConfig(map.getName()).getTotalBackupCount();
        return new MapStats(map, name, map.size(), totalBackups, showConfig);
    }
}
