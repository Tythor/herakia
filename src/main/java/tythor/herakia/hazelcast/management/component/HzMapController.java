package tythor.herakia.hazelcast.management.component;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tythor.herakia.hazelcast.management.dto.MapDto;

import java.util.List;

@RestController
@RequestMapping(path = "/hz/map/{name}")
@RequiredArgsConstructor
public class HzMapController {
    private final HzMapService hzMapService;

    @GetMapping
    public List<MapDto> getMapDtos(@RequestParam(name = "members", required = false) List<String> memberHostnames,
                                   @PathVariable(name = "name") String mapName,
                                   @RequestParam(name = "showKeys", required = false) boolean showKeys,
                                   @RequestParam(name = "showEntrySet", required = false) boolean showEntrySet,
                                   @RequestParam(name = "showConfig", required = false) boolean showConfig) {
        return hzMapService.getMapDtos(memberHostnames, mapName, showKeys, showEntrySet, showConfig);
    }

    @DeleteMapping
    public void clearMap(@PathVariable(name = "name") String mapName) {
        hzMapService.clearMap(mapName);
    }

    @GetMapping(path = "/entry", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getEntry(@PathVariable(name = "name") String mapName,
                           @RequestParam("key") String keyString,
                           @RequestParam("keyClass") Class<?> keyClass,
                           @RequestParam(name = "showStats", required = false) boolean showStats) {
        return hzMapService.getEntry(mapName, keyString, keyClass, showStats);
    }

    @DeleteMapping("/entry")
    public void removeEntry(@PathVariable(name = "name") String mapName,
                            @RequestParam("key") String keyString,
                            @RequestParam("keyClass") Class<?> keyClass) {
        hzMapService.deleteEntry(mapName, keyString, keyClass);
    }
}
