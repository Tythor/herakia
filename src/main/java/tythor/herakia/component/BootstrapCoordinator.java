package tythor.herakia.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tythor.herakia.configuration.AbstractBootstrapService;
import tythor.herakia.utility.SpringUtil;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapCoordinator implements AbstractBootstrapService {
    private final List<AbstractBootstrapService> bootstrapServices;

    @EventListener(ContextRefreshedEvent.class)
    @Override
    public void preStartup() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info("Bootstrapping {}() methods...", methodName);
        logSeparator();

        for (AbstractBootstrapService service : bootstrapServices) {
            log.info("{}.{}()...", service.getClass().getSimpleName(), methodName);
            service.preStartup();
            logSeparator();
        }

        log.info("Completed {}()!", methodName);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void postStartup() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info("Bootstrapping {}() methods...", methodName);
        logSeparator();

        for (AbstractBootstrapService service : bootstrapServices) {
            log.info("{}.{}()...", service.getClass().getSimpleName(), methodName);
            service.postStartup();
            logSeparator();
        }

        log.info("Completed {}()!", methodName);
    }

    private static void logSeparator() {
        int applicationNameLength = SpringUtil.getBean(Environment.class).getProperty("spring.application.name").length();
        System.out.println("-".repeat(108 + applicationNameLength) + ":");
    }
}

