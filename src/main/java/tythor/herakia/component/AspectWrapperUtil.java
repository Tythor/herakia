package tythor.herakia.component;

import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import tythor.herakia.annotation.executiontime.ExecutionTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.lock.LockFactory;

import java.util.function.Supplier;

@Slf4j
@Component
public class AspectWrapperUtil {
    @Async
    public void runAsync(Runnable runnable) {
        runnable.run();
    }

    @Async
    public <T> T supplyAsync(Supplier<T> supplier) {
        return supplier.get();
    }

    @Transactional
    public void runTransactional(Runnable runnable) {
        runnable.run();
    }

    @Transactional
    public <T> T supplyTransactional(Supplier<T> supplier) {
        return supplier.get();
    }
}
