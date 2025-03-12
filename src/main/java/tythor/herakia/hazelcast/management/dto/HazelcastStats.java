package tythor.herakia.hazelcast.management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"memberCount", "pingStats", "distributedObjectDtos"})
public class HazelcastStats {
    private int memberCount;

    @JsonProperty("pingStats")
    private List<PingStats> pingStatsList;

    private List<DistributedObjectDto> distributedObjectDtos;
}
