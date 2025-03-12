package tythor.herakia.utility;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.web.client.RestTemplate;
import tythor.herakia.bucket.BandwidthType;
import tythor.herakia.bucket.BucketChain;
import tythor.herakia.bucket.BucketType;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class Bucket4jUtil {
//    private static final Bandwidth BLOCKING_SPEED_LIMIT = Bandwidth.builder().capacity(1).refillIntervallyAligned(1, Duration.ofMillis(1), Instant.EPOCH).build(); // GLOBAL SPEED LIMIT - BURST PROTECTION - INACCURATE
    private static final Bandwidth BLOCKING_SPEED_LIMIT = Bandwidth.builder().capacity(1).refillIntervallyAligned(1, Duration.ofNanos(100_000), Instant.EPOCH).build(); // GLOBAL SPEED LIMIT - BURST PROTECTION - INACCURATE

    private static final Bandwidth LIMIT_PER_SECOND = BandwidthType.INTERVALLY.getBandwidth(100, Duration.ofSeconds(1));
    private static final Bandwidth LIMIT_PER_MINUTE = BandwidthType.INTERVALLY.getBandwidth(1000, Duration.ofMinutes(1));
    private static final Bandwidth LIMIT_PER_HOUR = BandwidthType.INTERVALLY.getBandwidth(10000, Duration.ofHours(1));
//    private static final Bandwidth LIMIT_PER_HOUR = Bandwidth.builder().capacity(1).refillIntervally(10000, Duration.ofHours(1)).build();

    private static final Map<String, Bucket> LOCAL_BUCKET_MAP = new ConcurrentHashMap<>();
    private static final Map<BucketType, Map<String, Bucket>> BUCKET_MAP = new ConcurrentHashMap<>();
    private static final Map<BucketType, Map<String, BucketChain>> BUCKET_CHAIN_MAP = new ConcurrentHashMap<>();


    public static Bucket getBucket(BucketType bucketType, String key, Bandwidth bandwidth) {
        return BUCKET_MAP.computeIfAbsent(bucketType, k -> new ConcurrentHashMap<>()).computeIfAbsent(key, k -> createBucket(bucketType, key, bandwidth));
    }

    public static BucketChain getBucketChain(BucketType bucketType, String key, Bandwidth[] bandwidths) {
        // USE A BUILDER
        Function<String, BucketChain> createBucketChainFunction = k -> new BucketChain(bucketType, key, bandwidths, BLOCKING_SPEED_LIMIT, Bucket4jUtil::getBucket);
        return BUCKET_CHAIN_MAP.computeIfAbsent(bucketType, k -> new ConcurrentHashMap<>()).computeIfAbsent(key, createBucketChainFunction);
    }

    private static Bucket createBucket(BucketType bucketType, String key, Bandwidth bandwidth) {
        return switch (bucketType) {
            case LOCAL -> createLocalBucket(key, bandwidth);
            case REDIS -> createRedisBucket(key, bandwidth);
            case HAZELCAST -> createHazelcastBucket(key, bandwidth);
        };
    }

    private static Bucket createLocalBucket(String key, Bandwidth bandwidth) {
        Function<String, Bucket> createBucketFunction = k -> Bucket.builder()
                .addLimit(bandwidth)
                .build();
        return LOCAL_BUCKET_MAP.computeIfAbsent(key, createBucketFunction);
    }

    private static Bucket createRedisBucket(String key, Bandwidth bandwidth) {
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();

        CommandAsyncExecutor commandAsyncExecutor = RedisUtil.getCommandAsyncExecutor();
        if (commandAsyncExecutor == null) throw new RuntimeException(BucketType.REDIS + " not available.");

        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                .withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)));

        RedissonBasedProxyManager<String> proxyManager = RedissonBasedProxyManager.builderFor(commandAsyncExecutor)
                .withClientSideConfig(clientSideConfig)
                .build();

        return proxyManager.builder().build(key, () -> configuration);
    }

    private static Bucket createHazelcastBucket(String key, Bandwidth bandwidth) {
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();

        HazelcastInstance hazelcastInstance = HazelcastUtil.getInstance();
        if (hazelcastInstance == null) throw new RuntimeException(BucketType.HAZELCAST + " not available.");

        IMap<String, byte[]> map = hazelcastInstance.getMap("bucket");

        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
            .withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)));

        HazelcastProxyManager<String> proxyManager = new HazelcastProxyManager<>(map, clientSideConfig, Offloadable.OFFLOADABLE_EXECUTOR);

        return proxyManager.builder().build(key, () -> configuration);
    }

    public static RRateLimiter getRedisRateLimiter() {
        RRateLimiter limiter = RedisUtil.getClient().getRateLimiter("myLimiter");
        // Initialization required only once.

        // 5 permits per 2 seconds
        limiter.trySetRate(RateType.OVERALL, 10000, 1, RateIntervalUnit.SECONDS);
        limiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.MILLISECONDS);

        return limiter;
    }
}
