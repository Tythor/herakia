package tythor.herakia.bucket;

import io.github.bucket4j.Bandwidth;

import java.time.Duration;
import java.time.Instant;

public enum BandwidthType {
    GREEDY {
        @Override
        public Bandwidth getBandwidth(int tokens, Duration period) {
            return Bandwidth.builder()
                .capacity(tokens)
                .refillGreedy(tokens, period)
                .build();
        }
    },
    INTERVALLY {
        @Override
        public Bandwidth getBandwidth(int tokens, Duration period) {
            return Bandwidth.builder()
                .capacity(tokens)
                .refillIntervallyAligned(tokens, period, Instant.EPOCH)
                .build();
        }
    };

    public abstract Bandwidth getBandwidth(int tokens, Duration period);
}
