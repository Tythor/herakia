package tythor.herakia.hazelcast.management.callback;

import tythor.herakia.hazelcast.management.dto.PingStats;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.MultiExecutionCallback;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tythor.herakia.utility.DigitUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class TimestampCallback implements MultiExecutionCallback {
    public static final int TIMEOUT = 10;
    private final long startTime = System.nanoTime();
    private final List<PingStats> pingStatsList = new ArrayList<>();
    private final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

    @Override
    public synchronized void onResponse(Member member, Object value) {
        BigDecimal ping = new BigDecimal(DigitUtil.truncateDouble((System.nanoTime() - startTime) / 1_000_000d, 3));
        pingStatsList.add(new PingStats(member, ping));
    }

    @Override
    public void onComplete(Map<Member, Object> values) {
        completableFuture.complete(null);
    }

    public List<PingStats> acquirePingStats() {
        try {
            completableFuture.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("Hazelcast ping timed out.");
        }
        return pingStatsList;
    }
}
