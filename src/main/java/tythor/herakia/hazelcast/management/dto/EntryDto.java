package tythor.herakia.hazelcast.management.dto;

import lombok.Data;

@Data
public class EntryDto {
    private EntryStats entryStats;
    private Object entryValue;
    private Class<?> entryValueClass;
}
