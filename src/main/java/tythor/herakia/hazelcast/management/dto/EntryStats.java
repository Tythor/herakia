package tythor.herakia.hazelcast.management.dto;

import com.hazelcast.core.EntryView;
import lombok.Data;

import java.time.Instant;

@Data
public class EntryStats {
    private Instant creationTime;
    private Instant updatedTime;
    private Instant expirationTime;
    private long cost;
    private long ttl;
    private long maxIdle;
    private boolean isLocked;

    public <K, V> EntryStats(EntryView<K, V> entryView, boolean isLocked) {
        this.creationTime = entryView.getCreationTime() == -1 ? null : Instant.ofEpochMilli(entryView.getCreationTime());
        this.updatedTime = entryView.getLastUpdateTime() == -1 ? null : Instant.ofEpochMilli(entryView.getLastUpdateTime());
        this.expirationTime = entryView.getExpirationTime() == Long.MAX_VALUE ? null : Instant.ofEpochMilli(entryView.getExpirationTime());
        this.cost = entryView.getCost();
        this.ttl = entryView.getTtl();
        this.maxIdle = entryView.getMaxIdle();
        this.isLocked = isLocked;
    }
}
