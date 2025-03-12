package tythor.herakia.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tythor.herakia.configuration.AbstractBootstrapService;
import tythor.herakia.utility.SignatureUtil;
import tythor.herakia.utility.SpringUtil;

import java.time.ZoneId;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapCoordinator implements AbstractBootstrapService {
    private final ObjectProvider<AbstractBootstrapService> bootstrapServices;

    @EventListener(ContextRefreshedEvent.class)
    @Override
    public void preStartup() {
        runStartupMethod(AbstractBootstrapService::preStartup);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void postStartup() {
        runStartupMethod(AbstractBootstrapService::postStartup);
    }

    private void runStartupMethod(Consumer<AbstractBootstrapService> consumer) {
        if (bootstrapServices.stream().allMatch(x -> x.equals(this))) return;

        String methodName = SignatureUtil.getMethodName(3);

        log.info("Bootstrapping {}() methods...", methodName);
        logSeparator();

        for (AbstractBootstrapService service : bootstrapServices) {
            log.info("{}.{}()...", service.getClass().getSimpleName(), methodName);
            consumer.accept(service);
            logSeparator();
        }

        log.info("Completed {}()!", methodName);
    }


    private static void logSeparator() {
        int applicationNameLength = SpringUtil.getBean(Environment.class).getProperty("spring.application.name").length();
        int timeZoneLength = ZoneId.systemDefault().equals(ZoneId.of("UTC")) ? 1 : 6;
        System.out.println("-".repeat(102 + applicationNameLength + timeZoneLength) + ":");
    }
}

