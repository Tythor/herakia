package tythor.herakia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tythor.herakia.utility.ApplicationUtil;
import tythor.herakia.utility.LoggingUtil;

@SpringBootApplication
public class HerakiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HerakiaApplication.class, args);
    }

    public static ConfigurableApplicationContext run(Class<?> primarySource, String[] args) {
        LoggingUtil.logSystemInformation();

        SpringApplication springApplication = new SpringApplication(primarySource);

        if (System.getenv("KUBERNETES_SERVICE_HOST") != null) springApplication.setAdditionalProfiles("k8s");
        springApplication.addInitializers(ApplicationUtil::setApplicationContext);

        ConfigurableApplicationContext applicationContext = springApplication.run(args);

//        Runtime.getRuntime().addShutdownHook(new Thread(payment.router.utility.ApplicationUtil::terminate));

        LoggingUtil.logApplicationInformation();

        return applicationContext;
    }

}
