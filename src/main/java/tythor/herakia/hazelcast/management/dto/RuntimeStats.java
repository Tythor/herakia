package tythor.herakia.hazelcast.management.dto;

import lombok.Data;

@Data
public class RuntimeStats {
    private int cpus;        // Number of logical cores
    private long maxMem;     // Max memory the JVM is allowed to allocate
    private long freeMem;    // Memory allocated but not used
    private long totalMem;   // Total memory already allocated but not necessarily used nor free
    private long usedMem;    // Memory used
    private long unallocMem; // Memory left to be allocated until it hits the max

    public RuntimeStats() {
        Runtime runtime = Runtime.getRuntime();

        this.cpus = runtime.availableProcessors();
        this.maxMem = runtime.maxMemory();
        this.freeMem = runtime.freeMemory();
        this.totalMem = runtime.totalMemory();
        this.usedMem = runtime.totalMemory() - runtime.freeMemory();
        this.unallocMem = runtime.maxMemory() - runtime.totalMemory();
    }
}
