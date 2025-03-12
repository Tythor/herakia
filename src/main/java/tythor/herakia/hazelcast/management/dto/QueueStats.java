package tythor.herakia.hazelcast.management.dto;

import tythor.herakia.hazelcast.management.enumeration.DistributedObjectType;
import lombok.Data;

@Data
public class QueueStats extends DistributedObjectStats {
    public QueueStats(String name, int size, int totalBackups) {
        super(DistributedObjectType.QUEUE, name, size, totalBackups);
    }

    @Override
    public DistributedObjectStats mergeDistributedObjectStats(DistributedObjectStats distributedObjectStats) {
        return null;
    }
}
