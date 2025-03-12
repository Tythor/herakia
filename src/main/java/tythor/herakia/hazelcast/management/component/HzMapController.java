package tythor.herakia.hazelcast.management.component;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tythor.herakia.hazelcast.management.dto.DistributedObjectDto;
import tythor.herakia.hazelcast.management.enumeration.QueryType;
import tythor.herakia.utility.RequestContextUtil;

import java.util.List;

@RestController
@RequestMapping(path = "/hz/map/{name}")
@RequiredArgsConstructor
public class HzMapController {
    private final HazelcastStatsCollector hazelcastStatsCollector;
    private final HzMapService hzMapService;

    @GetMapping
    public List<DistributedObjectDto> getMapDtos(@PathVariable(name = "name") String mapName,
                                                 @RequestParam(name = "queryType", required = false, defaultValue = "LOCAL") QueryType queryType,
                                                 @RequestParam(name = "showConfig", required = false) boolean showConfig,
                                                 @RequestParam(name = "showKeys", required = false) boolean showKeys,
                                                 @RequestParam(name = "showValues", required = false) boolean showValues,
                                                 @RequestParam(name = "showMap", required = false) boolean showMap) {
        hzMapService.validateMapExists(mapName);

        RequestContextUtil.set("showConfig", showConfig);
        RequestContextUtil.set("mapName", mapName);
        RequestContextUtil.set("showKeys", showKeys);
        RequestContextUtil.set("showValues", showValues);
        RequestContextUtil.set("showMap", showMap);
        return hazelcastStatsCollector.getDistributedObjectInfoList(queryType);
    }

    @DeleteMapping
    public void clearMap(@PathVariable(name = "name") String mapName) {
        hzMapService.validateMapExists(mapName);
        hzMapService.clearMap(mapName);
    }

    @GetMapping(path = "/entry", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getEntry(@PathVariable(name = "name") String mapName,
                           @RequestParam("key") String keyString,
                           @RequestParam("keyClass") Class<?> keyClass,
                           @RequestParam(name = "showStats", required = false) boolean showStats) {
        hzMapService.validateMapExists(mapName);
        return hzMapService.getEntry(mapName, keyString, keyClass, showStats);
    }

    @DeleteMapping("/entry")
    public void removeEntry(@PathVariable(name = "name") String mapName,
                            @RequestParam("key") String keyString,
                            @RequestParam("keyClass") Class<?> keyClass) {
        hzMapService.validateMapExists(mapName);
        hzMapService.deleteEntry(mapName, keyString, keyClass);
    }
}
