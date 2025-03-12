package tythor.herakia.hazelcast.management.dto;

import tythor.herakia.hazelcast.management.enumeration.DistributedObjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class DistributedObjectStats implements Serializable {
    private DistributedObjectType type;
    private String name;
    private int size;
    private int totalBackups;

    public abstract DistributedObjectStats mergeDistributedObjectStats(DistributedObjectStats distributedObjectStats);
}
