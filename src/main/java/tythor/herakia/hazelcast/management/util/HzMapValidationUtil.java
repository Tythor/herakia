package tythor.herakia.hazelcast.management.util;

import com.hazelcast.map.impl.proxy.MapProxyImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tythor.herakia.utility.HazelcastUtil;

public class HzMapValidationUtil {
    public static void validateMapExists(String mapName) {
        boolean mapExists = HazelcastUtil.getInstance().getDistributedObjects().stream().anyMatch(x -> x instanceof MapProxyImpl && x.getName().equals(mapName));

        if (!mapExists) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map does not exist.");
    }
}
