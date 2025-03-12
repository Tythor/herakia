package tythor.herakia.hazelcast.management.dto;

import tythor.herakia.hazelcast.management.util.MapClassCacheUtil;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.map.IMap;
import lombok.Data;
import tythor.herakia.utility.HazelcastUtil;

import java.io.Serializable;

@Data
public class MapConfigStats implements Serializable {
    private final Class<?> keyClass;
    private final Class<?> valueClass;

    private final InMemoryFormat inMemoryFormat;
    private final int backupCount;
    private final int asyncBackupCount;
    private final int timeToLiveSeconds;
    private final int maxIdleSeconds;

    public MapConfigStats(IMap<?, ?> map) {
        MapClassCacheUtil.MapClass mapClass = MapClassCacheUtil.getMapClass(map);
        this.keyClass = mapClass != null ? mapClass.getKeyClass() : null;
        this.valueClass = mapClass != null ? mapClass.getValueClass() : null;

        MapConfig mapConfig = HazelcastUtil.getInstance().getConfig().getMapConfig(map.getName());
        this.inMemoryFormat = mapConfig.getInMemoryFormat();
        this.backupCount = mapConfig.getBackupCount();
        this.asyncBackupCount = mapConfig.getAsyncBackupCount();
        this.timeToLiveSeconds = mapConfig.getTimeToLiveSeconds();
        this.maxIdleSeconds = mapConfig.getMaxIdleSeconds();
    }
}
