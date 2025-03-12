package tythor.herakia.hazelcast.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MapDto extends DistributedObjectDto {
    private Set<?> keys;
    private Set<? extends Map.Entry<?, ?>> entrySet;
}
