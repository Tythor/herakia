package tythor.herakia.bucket;

import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;

@AllArgsConstructor
public class BlockingBucketDelegate implements Bucket {
    private final Bucket delegateBucket;
    private final Bucket blockingBucket;

    private ConsumptionProbe latestConsumptionProbe;

    @Override
    public BlockingBucket asBlocking() {
        return delegateBucket.asBlocking();
    }

    @Override
    public SchedulingBucket asScheduler() {
        return delegateBucket.asScheduler();
    }

    @Override
    public VerboseBucket asVerbose() {
        return delegateBucket.asVerbose();
    }

    @SneakyThrows
    @Override
    public boolean tryConsume(long numTokens) {
        blockingBucket.asBlocking().consume(numTokens);
        return delegateBucket.tryConsume(numTokens);
    }

    @Override
    public long consumeIgnoringRateLimits(long tokens) {
        return delegateBucket.consumeIgnoringRateLimits(tokens);
    }

    @SneakyThrows
    @Override
    public ConsumptionProbe tryConsumeAndReturnRemaining(long numTokens) {
        blockingBucket.asBlocking().consume(numTokens);
        this.latestConsumptionProbe = delegateBucket.tryConsumeAndReturnRemaining(numTokens);
        return latestConsumptionProbe;
    }

    @Override
    public EstimationProbe estimateAbilityToConsume(long numTokens) {
        return delegateBucket.estimateAbilityToConsume(numTokens);
    }

    @Override
    public long tryConsumeAsMuchAsPossible() {
        return delegateBucket.tryConsumeAsMuchAsPossible();
    }

    @Override
    public long tryConsumeAsMuchAsPossible(long limit) {
        return delegateBucket.tryConsumeAsMuchAsPossible(limit);
    }

    @Override
    public void addTokens(long tokensToAdd) {
        delegateBucket.addTokens(tokensToAdd);
    }

    @Override
    public void forceAddTokens(long tokensToAdd) {
        delegateBucket.forceAddTokens(tokensToAdd);
    }

    @Override
    public void reset() {
        delegateBucket.reset();
    }

    @Override
    public long getAvailableTokens() {
        return delegateBucket.getAvailableTokens();
    }

    @Override
    public void replaceConfiguration(BucketConfiguration newConfiguration, TokensInheritanceStrategy tokensInheritanceStrategy) {
        delegateBucket.replaceConfiguration(newConfiguration, tokensInheritanceStrategy);
    }

    @Override
    public Bucket toListenable(BucketListener listener) {
        return delegateBucket.toListenable(listener);
    }

    public void addRateLimitHeaders(HttpServletResponse response) {
        long remaining = latestConsumptionProbe.getRemainingTokens();
        long reset = Duration.ofNanos(latestConsumptionProbe.getNanosToWaitForReset()).toSeconds();
        long retryAfter = Duration.ofNanos(latestConsumptionProbe.getNanosToWaitForRefill()).toSeconds();

        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
        response.addHeader("X-Rate-Limit-Reset", String.valueOf(reset));
        response.addHeader("X-Rate-Limit-Retry-After", String.valueOf(retryAfter));
    }
}
