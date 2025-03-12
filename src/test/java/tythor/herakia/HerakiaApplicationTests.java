package tythor.herakia;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tythor.herakia.configuration.HerakiaHazelcastConfig;
import tythor.herakia.exception.LockedException;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.lock.LockFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class HerakiaApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) throws LockedException {
        LockFactory.builder(LockFactory.POSTGRES).tryLock().task(() -> {
            System.out.println();
        }).execute();


        Hazelcast.newHazelcastInstance();
        IMap<String, Object> lockMap = HazelcastUtil.getMap(HerakiaHazelcastConfig.LOCK_MAP);
        LockFactory.builder()
            .lockMap(lockMap)
            .lockKey(0)
            .tryLock(Duration.ofSeconds(1), Duration.ofSeconds(5))
            .task(() -> System.out.println()).execute();

        int a = 0;
    }

}
