package tythor.herakia.bucket;

import com.hazelcast.jet.function.TriFunction;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

public class BucketChain {
    private Bucket blockingBucket;
    private final List<Bucket> bucketList = new ArrayList<>();
    private final Map<Bucket, Bandwidth> bandwidthMap = new HashMap<>();

    public BucketChain(BucketType bucketType, String key, Bandwidth[] bandwidths, Bandwidth blockingBandwidth, TriFunction<BucketType, String, Bandwidth, Bucket> bucketProvider) {
        // Sort by most to least restrictive for efficiency
        List<Bandwidth> sortedBandwidths = Stream.concat(Stream.of(blockingBandwidth), Stream.of(bandwidths)).sorted(Comparator.comparing(x -> x.getRefillPeriodNanos() * x.getRefillTokens())).toList();

        for (int i = 0; i < sortedBandwidths.size(); i++) {
            Bandwidth bandwidth = sortedBandwidths.get(i);
            Bucket bucket = bucketProvider.apply(bucketType, key + i, bandwidth);
            if (bandwidth.equals(blockingBandwidth)) {
                blockingBucket = bucket;
            } else {
                bucketList.add(bucket);
                bandwidthMap.put(bucket, bandwidth);
            }
        }
    }

    public void blockingConsume(long numTokens) throws InterruptedException {
        blockingBucket.asBlocking().consume(numTokens);
        for (Bucket bucket : bucketList) {
            bucket.asBlocking().consume(numTokens);
        }
    }

    public boolean tryConsume(long numTokens) throws InterruptedException {
        blockingBucket.asBlocking().consume(numTokens);
        return recursiveConsumeAndRefund(numTokens, bucketList.iterator());
    }

    private boolean recursiveConsumeAndRefund(long numTokens, Iterator<Bucket> iterator) {
        if (!iterator.hasNext()) return true;

        Bucket bucket = iterator.next();
        if (!bucket.tryConsume(numTokens)) return false;

        boolean result = recursiveConsumeAndRefund(numTokens, iterator);

        if (!result) {
            bucket.addTokens(numTokens);
        }

        return result;
    }

    public BucketChainResult tryConsumeAndReturnRemaining(long numTokens) throws InterruptedException {
        blockingBucket.asBlocking().consume(numTokens);
        return recursiveConsumeAndReturnRemainingAndRefund(numTokens, bucketList.iterator(), new BucketChainResult());
    }

    private BucketChainResult recursiveConsumeAndReturnRemainingAndRefund(long numTokens, Iterator<Bucket> iterator, BucketChainResult bucketChainResult) {
        if (!iterator.hasNext()) return null;

        Bucket bucket = iterator.next();
        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(numTokens);
        ConsumptionProbeBandwidth consumptionProbeBandwidth = new ConsumptionProbeBandwidth(consumptionProbe, bandwidthMap.get(bucket));
        bucketChainResult.add(consumptionProbeBandwidth);

//            if (consumptionProbe.isConsumed()) // early return with missing headers
            recursiveConsumeAndReturnRemainingAndRefund(numTokens, iterator, bucketChainResult);

        if (consumptionProbe.isConsumed() && !bucketChainResult.isConsumed())
            bucket.addTokens(numTokens);

        return bucketChainResult;
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
    
    @Data
    public static class BucketChainResult {
        private List<ConsumptionProbeBandwidth> consumptionProbeBandwidthList = new ArrayList<>();

        public void add(ConsumptionProbeBandwidth consumptionProbeBandwidth) {
            consumptionProbeBandwidthList.add(consumptionProbeBandwidth);
        }

        public boolean isConsumed() {
            return consumptionProbeBandwidthList.stream().allMatch(x -> x.getConsumptionProbe().isConsumed());
        }

        public void addRateLimitHeaders(HttpServletResponse response) {
            if (consumptionProbeBandwidthList.isEmpty()) return;

            String limit = "", remaining = "", reset = "", retryAfter = "";

            for (ConsumptionProbeBandwidth consumptionProbeBandwidth : consumptionProbeBandwidthList) {
                ConsumptionProbe consumptionProbe = consumptionProbeBandwidth.getConsumptionProbe();
                Bandwidth bandwidth = consumptionProbeBandwidth.getBandwidth();
                String window = consumptionProbeBandwidthList.size() > 1 ? String.format(";w=%d ", Duration.ofNanos(bandwidth.getRefillPeriodNanos()).toSeconds()) : "";

                limit += bandwidth.getCapacity() + window;
                remaining += consumptionProbe.getRemainingTokens() + window;
                reset += Duration.ofNanos(consumptionProbe.getNanosToWaitForReset()).toSeconds() + window;
                retryAfter += Duration.ofNanos(consumptionProbe.getNanosToWaitForRefill()).toSeconds() + window;
            }

            response.addHeader("X-Rate-Limit-Limit", limit);
            response.addHeader("X-Rate-Limit-Remaining", remaining);
            response.addHeader("X-Rate-Limit-Reset", reset);
            response.addHeader("X-Rate-Limit-Retry-After", retryAfter);
        }
    }

    @Data
    @AllArgsConstructor
    public static class ConsumptionProbeBandwidth {
        private ConsumptionProbe consumptionProbe;
        private Bandwidth bandwidth;
    }
}
