package tythor.herakia.hazelcast.management.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import tythor.herakia.hazelcast.management.dto.DistributedObjectDto;
import tythor.herakia.hazelcast.management.dto.HazelcastStats;
import tythor.herakia.hazelcast.management.dto.PingStats;
import tythor.herakia.hazelcast.management.dto.RuntimeStats;
import tythor.herakia.hazelcast.management.enumeration.QueryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.JacksonUtil;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/hz")
@RequiredArgsConstructor
public class HzController {
    private final ObjectMapper nonReflectionObjectMapper = JacksonUtil.getNonReflectionObjectMapper();
    private final HazelcastStatsCollector hazelcastStatsCollector;

    @GetMapping("/stats")
    public HazelcastStats getHazelcastStats(@RequestParam(name = "queryType", required = false, defaultValue = "LOCAL") QueryType queryType) {
        return hazelcastStatsCollector.getHazelcastStats(queryType);
    }

    @GetMapping("/objects")
    public List<DistributedObjectDto> getDistributedObjectDtoList(@RequestParam(name = "queryType", required = false, defaultValue = "LOCAL") QueryType queryType) {
        return hazelcastStatsCollector.getDistributedObjectInfoList(queryType);
    }

    @GetMapping("/ping")
    public List<PingStats> getPingStats() {
        return hazelcastStatsCollector.getPingStats(HazelcastUtil.getInstance().getCluster().getMembers());
    }

    @GetMapping(path = "/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMembers() throws JsonProcessingException {
        return nonReflectionObjectMapper.writeValueAsString(HazelcastUtil.getInstance().getCluster().getMembers());
    }

    @GetMapping("/rt")
    public RuntimeStats getRuntimeStats() {
        return new RuntimeStats();
    }

    @GetMapping("/shutdown")
    public void shutdownEndpoint() {
        HazelcastUtil.shutdown();
    }

    @GetMapping("/gc")
    public void gc() {
        log.info("Running garbage collection...");
        Runtime.getRuntime().gc();
    }
}
