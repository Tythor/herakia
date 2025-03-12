package tythor.herakia.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;

@Slf4j
@Component
@ConditionalOnProperty("server.port")
public class WebServerConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Value("${server.port}")
    private int serverPort;

    @Override
    public void customize(ConfigurableServletWebServerFactory configurableServletWebServerFactory) {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
        } catch (Exception e) {
            log.info("Found another process on default port {}. Starting web server on port 0 instead...", serverPort);
            configurableServletWebServerFactory.setPort(0);
        }
    }
}
