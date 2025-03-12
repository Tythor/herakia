package tythor.herakia.hazelcast.management.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import tythor.herakia.hazelcast.management.callable.ObjectStatsCollector;
import tythor.herakia.hazelcast.management.callback.TimestampCallback;
import tythor.herakia.hazelcast.management.dto.*;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.proxy.MapProxyImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tythor.herakia.utility.HazelcastUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HzMapService {
    private final HzKeyResolver hzKeyResolver;
    private final ObjectMapper objectMapper;

    public List<MapDto> getMapDtos(List<String> memberHostnames, String mapName, boolean showKeys, boolean showEntrySet, boolean showConfig) {
        Set<Member> members = new HashSet<>(HazelcastUtil.getInstance().getCluster().getMembers());
        if (memberHostnames != null && !memberHostnames.isEmpty()) {
            members.removeIf(x ->  x.getAttribute("hostname") == null || !memberHostnames.contains(x.getAttribute("hostname")));
            if (members.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching members found.");
            }
        }

        return members.stream().map(member -> createMapDto(member, mapName, showKeys, showEntrySet, showConfig)).sorted(Comparator.comparing(x -> x.getOwner())).toList();
    }

    private MapDto createMapDto(Member member, String mapName, boolean showKeys, boolean showEntrySet, boolean showConfig) {
        boolean mapExists = HazelcastUtil.getInstance().getDistributedObjects().stream().anyMatch(x -> x instanceof MapProxyImpl && x.getName().equals(mapName));
        if (!mapExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Map does not exist.");
        }

        IMap<?, ?> map = HazelcastUtil.getMap(mapName);
        MapDto mapDto = new MapDto();

        boolean tooBig = false;
        MapStats mapStats = getMapStats(member, map.getName(), showConfig);
        if (mapStats != null) {
            mapDto.setDistributedObjectStatsList(List.of(mapStats));

            long totalEntrySetSize = mapStats.getEntryCost() * HazelcastUtil.getInstance().getCluster().getMembers().size();
            double maxMemoryThreshold = Runtime.getRuntime().maxMemory() * 0.5;
            tooBig = totalEntrySetSize > maxMemoryThreshold;
        }

        if (showKeys && !tooBig) {
            mapDto.setKeys(map.keySet());
        }

        if (showEntrySet && !tooBig) {
            mapDto.setEntrySet(map.entrySet());
        }

        mapDto.setOwner(member.getAttribute("hostname"));
        return mapDto;
    }

    public MapStats getMapStats(Member member, String mapName, boolean showConfig) {
        HazelcastInstance hazelcastInstance = HazelcastUtil.getInstance();
        IExecutorService executorService = hazelcastInstance.getExecutorService("defaultExecutorService");
        IMap<?, ?> map = HazelcastUtil.getMap(mapName);

        MapStats mapStats = null;
        ObjectStatsCollector<MapStats> task = new ObjectStatsCollector<>(MapStats.class, map.getName(), showConfig);
        Future<MapStats> future = executorService.submitToMember(task, member);

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

        /*JsonMapper objectMapper = JsonMapper.builder()
            .disable(MapperFeature.AUTO_DETECT_GETTERS)
            .enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .addModule(new JavaTimeModule()).build();*/

        return objectMapper.writeValueAsString(entryDto);
    }

    private EntryDto getEntryDto(String mapName, String keyString, Class<?> keyClass, boolean showStats) {
        EntryDto entryDto = new EntryDto();
        IMap<Object, ?> map = HazelcastUtil.getMap(mapName);
        Object key = hzKeyResolver.resolve(keyString, keyClass);

        if (!map.containsKey(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Key=" + keyString + " not found in Map=" + mapName);
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
