package tythor.herakia.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import tythor.herakia.configuration.AbstractBootstrapService;
import tythor.herakia.utility.SignatureUtil;

import java.time.ZoneId;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapOrchestrator implements AbstractBootstrapService {
    @Value("${spring.application.name:}")
    private String applicationName;

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

        String methodName = SignatureUtil.getMethodName(2);

        log.info("Bootstrapping {}() methods...", methodName);
        logSeparator();

        for (AbstractBootstrapService service : bootstrapServices) {
            log.info("{}.{}()...", ClassUtils.getUserClass(service).getSimpleName(), methodName);
            consumer.accept(service);
            logSeparator();
        }

        log.info("Completed {}()!", methodName);
    }

    private void logSeparator() {
        int applicationNameLength = !applicationName.isBlank() ? applicationName.length() + 3 : 0; // [applicationName]
        int timeZoneLength = ZoneId.systemDefault().equals(ZoneId.of("UTC")) ? 1 : 6;
        int pid = String.valueOf(ProcessHandle.current().pid()).length();
        System.out.println("-".repeat(94 + applicationNameLength + timeZoneLength + pid) + ":");
    }
}

