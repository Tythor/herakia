package tythor.herakia.hazelcast.management.dto;

import lombok.NoArgsConstructor;
import tythor.herakia.hazelcast.management.util.MapClassCacheUtil;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.map.IMap;
import lombok.Data;
import tythor.herakia.utility.HazelcastUtil;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class MapConfigStats implements Serializable {
    private Class<?> keyClass;
    private Class<?> valueClass;

    private InMemoryFormat inMemoryFormat;
    private int backupCount;
    private int asyncBackupCount;
    private int timeToLiveSeconds;
    private int maxIdleSeconds;

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
