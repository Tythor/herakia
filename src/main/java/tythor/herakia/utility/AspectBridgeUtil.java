package tythor.herakia.utility;

import lombok.extern.slf4j.Slf4j;
import tythor.herakia.annotation.ExecutionTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Slf4j
@Component
public class AspectBridgeUtil {
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

    // Note: The aspect overwrites the method signature
    @ExecutionTime
    public void runExecutionTime(Runnable runnable) {
        runnable.run();
    }

    // Note: The aspect overwrites the method signature
    @ExecutionTime
    public <T> T supplyExecutionTime(Supplier<T> supplier) {
        return supplier.get();
    }
}
