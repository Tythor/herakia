package tythor.herakia.hazelcast.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tythor.herakia.hazelcast.management.enumeration.DistributedObjectType;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.SpringUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MapStats extends DistributedObjectStats {
    private long totalEntries;
    private long entries;
    private long backupEntries;
    private long lockedEntries;
    private long totalCost;
    private long entryCost;
    private long backupCost;
    private long readCount;
    private long writeCount;
    private long deleteCount;

    private MapConfigStats mapConfigStats;
    private Set<?> keys;
    private List<?> values;
    private Map<?, ?> map;

    public MapStats(IMap<?, ?> map, String name, int size, int totalBackups, boolean showConfig, boolean showKeys, boolean showValues, boolean showMap) {
        super(DistributedObjectType.MAP, name, size, totalBackups);

        LocalMapStats localMapStats = map.getLocalMapStats();
        this.totalEntries = localMapStats.getOwnedEntryCount() + localMapStats.getBackupEntryCount();
        this.entries = localMapStats.getOwnedEntryCount();
        this.backupEntries = localMapStats.getBackupEntryCount();
        this.lockedEntries = localMapStats.getLockedEntryCount();
        this.totalCost = localMapStats.getHeapCost();
        this.entryCost = localMapStats.getOwnedEntryMemoryCost();
        this.backupCost = localMapStats.getBackupEntryMemoryCost();
        this.readCount = localMapStats.getGetOperationCount();
        this.writeCount = localMapStats.getPutOperationCount() + localMapStats.getSetOperationCount();
        this.deleteCount = localMapStats.getRemoveOperationCount() + localMapStats.getExpirationCount() + localMapStats.getEvictionCount();

        setOptionalFields(map, showConfig, showKeys, showValues, showMap);
    }

    private void setOptionalFields(IMap<?, ?> map, boolean showConfig, boolean showKeys, boolean showValues, boolean showMap) {
        if (showConfig) {
            this.mapConfigStats = new MapConfigStats(map);
        }

        long totalEntrySize = entryCost * HazelcastUtil.getInstance().getCluster().getMembers().size();
        double maxMemoryThreshold = Runtime.getRuntime().maxMemory() * 0.5;

        if (totalEntrySize < maxMemoryThreshold) {
            if (showKeys || showValues || showMap) {
                Set<?> localKeys = new LinkedHashSet<>(map.localKeySet());
                if (showKeys) {
                    this.keys = localKeys;
                }

                if (showValues || showMap) {
                    Map<?, ?> localMap = map.entrySet(entry -> localKeys.contains(entry.getKey()))
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    if (showValues) {
                        this.values = createValuesWithKeys(localMap);
                    }

                    if (showMap) {
                        this.map = localMap;
                    }
                }
            }
        }
    }

    private List<?> createValuesWithKeys(Map<?, ?> localMap) {
        return localMap.entrySet().stream()
            .map(entry -> {
                Map<String, Object> entryMap = new LinkedHashMap<>();
                entryMap.put("__key", entry.getKey());
                try {
                    entryMap.putAll(SpringUtil.getBean(ObjectMapper.class).convertValue(entry.getValue(), new TypeReference<>() {}));
                } catch (Exception e) {
                    entryMap.put("__value", entry.getValue());
                }
                return entryMap;
            })
            .toList();
    }

    @Override
    public MapStats mergeDistributedObjectStats(DistributedObjectStats distributedObjectStats) {
        MapStats mapStats = (MapStats) distributedObjectStats;

        this.totalEntries += mapStats.totalEntries;
        this.entries += mapStats.entries;
        this.backupEntries += mapStats.backupEntries;
        this.lockedEntries += mapStats.lockedEntries;
        this.totalCost += mapStats.totalCost;
        this.entryCost += mapStats.entryCost;
        this.backupCost += mapStats.backupCost;
        this.readCount += mapStats.readCount;
        this.writeCount += mapStats.writeCount;
        this.deleteCount += mapStats.deleteCount;

        this.keys = Stream.concat(Stream.ofNullable(keys), Stream.ofNullable(mapStats.keys)).flatMap(x -> x.stream()).collect(Collectors.toSet());
        this.values = Stream.concat(Stream.ofNullable(values), Stream.ofNullable(mapStats.values)).flatMap(x -> x.stream()).toList();
        this.map = Stream.concat(Stream.ofNullable(map), Stream.ofNullable(mapStats.map))
            .flatMap(x -> x.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return this;
    }
}
