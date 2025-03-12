package tythor.herakia.hazelcast.management.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tythor.herakia.hazelcast.management.dto.DistributedObjectDto;
import tythor.herakia.hazelcast.management.enumeration.QueryType;
import tythor.herakia.hazelcast.management.util.HzMapTableUtil;
import tythor.herakia.hazelcast.management.util.HzMapValidationUtil;
import tythor.herakia.utility.thread.ThreadStorage;

import java.util.List;

@SuppressWarnings("removal")
@RestControllerEndpoint(id = "hz-map")
@RequiredArgsConstructor
public class HzMapController {
    private final HazelcastStatsCollector hazelcastStatsCollector;
    private final HzMapService hzMapService;

    @GetMapping("/{mapName}")
    public List<DistributedObjectDto> getMapDtos(@PathVariable(name = "mapName") String mapName,
                                                 @RequestParam(name = "queryType", required = false, defaultValue = "LOCAL") QueryType queryType,
                                                 @RequestParam(name = "showConfig", required = false) boolean showConfig,
                                                 @RequestParam(name = "showKeys", required = false) boolean showKeys,
                                                 @RequestParam(name = "showValues", required = false) boolean showValues,
                                                 @RequestParam(name = "showMap", required = false) boolean showMap) {
        HzMapValidationUtil.validateMapExists(mapName);

        ThreadStorage.put("mapName", mapName);
        ThreadStorage.put("showConfig", showConfig);
        ThreadStorage.put("showKeys", showKeys);
        ThreadStorage.put("showValues", showValues);
        ThreadStorage.put("showMap", showMap);

        return hazelcastStatsCollector.getDistributedObjectInfoList(queryType);
    }

    @DeleteMapping("/{mapName}")
    public void clearMap(@PathVariable(name = "mapName") String mapName) {
        HzMapValidationUtil.validateMapExists(mapName);
        hzMapService.clearMap(mapName);
    }

    @GetMapping(path = "/{mapName}/entry", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getEntry(@PathVariable(name = "mapName") String mapName,
                           @RequestParam("key") String keyString,
                           @RequestParam("keyClass") Class<?> keyClass,
                           @RequestParam(name = "showStats", required = false) boolean showStats) {
        HzMapValidationUtil.validateMapExists(mapName);
        return hzMapService.getEntry(mapName, keyString, keyClass, showStats);
    }

    @DeleteMapping("/{mapName}/entry")
    public void removeEntry(@PathVariable(name = "mapName") String mapName,
                            @RequestParam("key") String keyString,
                            @RequestParam("keyClass") Class<?> keyClass) {
        HzMapValidationUtil.validateMapExists(mapName);
        hzMapService.deleteEntry(mapName, keyString, keyClass);
    }

    @GetMapping("/{mapName}/table")
    public String printTable(@PathVariable(name = "mapName") String mapName) {
        HzMapValidationUtil.validateMapExists(mapName);
        return HzMapTableUtil.printTable(mapName);
    }
}
