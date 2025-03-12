package tythor.herakia.hazelcast.management.dto;

import tythor.herakia.hazelcast.management.enumeration.DistributedObjectType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import lombok.Data;

@Data
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

    public MapStats(IMap<?, ?> map, String name, int size, int totalBackups, boolean showConfig) {
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

        if (showConfig) {
            this.mapConfigStats = new MapConfigStats(map);
        }
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
        return this;
    }
}
