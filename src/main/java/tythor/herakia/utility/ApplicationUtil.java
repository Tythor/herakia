package tythor.herakia.utility;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;


@Slf4j
public class ApplicationUtil {
    @Getter
    @Setter
    private static ApplicationContext applicationContext;

    public static synchronized void shutdown() {
        try {
            // Spring exit
            log.info("Starting Spring Application shutdown...");
            SpringApplication.exit(applicationContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void terminate() {
        shutdown();

          // Graceful exit
//        log.info("Exiting system...");
//        System.exit(0);

        // Force exit
        log.info("Halting runtime...");
        Runtime.getRuntime().halt(0);

        // Spring-devtools restart
//        Restarter.getInstance().restart();
    }
}
