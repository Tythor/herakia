package tythor.herakia.hazelcast.management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DistributedObjectDto {
    private String owner;

    @JsonProperty("distributedObjectStats")
    private List<DistributedObjectStats> distributedObjectStatsList;
}
