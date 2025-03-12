package tythor.herakia.hazelcast.management.component;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import tythor.herakia.hazelcast.management.callable.ObjectStatsCollector;
import tythor.herakia.hazelcast.management.callback.TimestampCallback;
import tythor.herakia.hazelcast.management.dto.EntryDto;
import tythor.herakia.hazelcast.management.dto.EntryStats;
import tythor.herakia.hazelcast.management.dto.MapStats;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.thread.ThreadStorage;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HzMapService {
    private final HzKeyResolver hzKeyResolver;
    private final ObjectMapper objectMapper;

    public MapStats getMapStats(Member member, String mapName) {
        HazelcastInstance hazelcastInstance = HazelcastUtil.getInstance();
        IExecutorService executorService = hazelcastInstance.getExecutorService("defaultExecutorService");
        IMap<?, ?> map = HazelcastUtil.getMap(mapName);

        boolean showConfig = ThreadStorage.get("showConfig");
        boolean showKeys = ThreadStorage.get("showKeys");
        boolean showValues = ThreadStorage.get("showValues");
        boolean showMap = ThreadStorage.get("showMap");

        ObjectStatsCollector<MapStats> task = new ObjectStatsCollector<>(MapStats.class, map.getName(), showConfig, showKeys, showValues, showMap);
        Future<MapStats> future = executorService.submitToMember(task, member);

        MapStats mapStats = null;
        try {
            mapStats = future.get(TimestampCallback.TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Cluster wide operation for Map={} failed.", map.getName());
            e.printStackTrace();
        }

        return mapStats;
    }

    public void clearMap(String mapName) {
        HazelcastUtil.getMap(mapName).clear();
    }

    @SneakyThrows
    public String getEntry(String mapName, String keyString, Class<?> keyClass, boolean showStats) {
        EntryDto entryDto = getEntryDto(mapName, keyString, keyClass, showStats);
        return objectMapper.writeValueAsString(entryDto);
    }

    private <K> EntryDto getEntryDto(String mapName, String keyString, Class<K> keyClass, boolean showStats) {
        EntryDto entryDto = new EntryDto();
        IMap<K, ?> map = HazelcastUtil.getMap(mapName);
        K key = hzKeyResolver.resolve(keyString, keyClass);

        if (!map.containsKey(key)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Key=" + keyString + " not found in Map=" + mapName);
        }

        if (showStats) {
            EntryStats entryStats = new EntryStats(map.getEntryView(key), map.isLocked(key));
            entryDto.setEntryStats(entryStats);
        }

        Object entryValue = map.get(key);
        entryDto.setEntryValue(entryValue);
        entryDto.setEntryValueClass(entryValue.getClass());

        return entryDto;
    }

    public void deleteEntry(String mapName, String keyString, Class<?> keyClass) {
        Object key = hzKeyResolver.resolve(keyString, keyClass);
        HazelcastUtil.getMap(mapName).delete(key);
    }
}
