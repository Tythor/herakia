package tythor.herakia.utility;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Slf4j
public class HazelcastUtil {
    public static HazelcastInstance getInstance() {
        return Stream.concat(Hazelcast.getAllHazelcastInstances().stream(), HazelcastClient.getAllHazelcastClients().stream()).findAny().orElse(null);
    }

    public static <K, V> IMap<K, V> getMap(String mapName) {
        return getInstance().getMap(mapName);
    }

    public static void shutdown() {
        try {
            log.info("Starting Hazelcast shutdown...");
            getInstance().getLifecycleService().shutdown();
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
