package tythor.herakia.configuration;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import tythor.herakia.component.DatabaseInitializer;
import tythor.herakia.utility.ApplicationUtil;
import tythor.herakia.utility.LoggingUtil;

public class HerakiaApplicationListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent applicationStartingEvent) {
            LoggingUtil.logSystemInformation();
            if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
                applicationStartingEvent.getSpringApplication().setAdditionalProfiles("k8s");
            }
        } else if (event instanceof ApplicationPreparedEvent applicationPreparedEvent) {
            ApplicationUtil.setApplicationContext(applicationPreparedEvent.getApplicationContext());
            DatabaseInitializer.initializeDatabases(applicationPreparedEvent.getApplicationContext().getEnvironment());
        } else if (event instanceof ApplicationReadyEvent applicationReadyEvent) {
            LoggingUtil.logApplicationInformation();
        }
    }
}
