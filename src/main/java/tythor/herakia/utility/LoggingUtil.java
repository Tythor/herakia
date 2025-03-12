package tythor.herakia.utility;

import ch.qos.logback.classic.LoggerContext;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class LoggingUtil {
    /**
     * <b>Note:</b> Logs may be lost due to TOCTOU.
     * <p>
     * <b>Note:</b> Logger signature will be changed to this class.
     */
    public static void logIfAvailable(Level level, String message) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (context.isStarted()) {
            // Logback is still alive
            log.atLevel(level).log(message);
        } else {
            // Logback has been stopped
            System.out.println(message);
        }
    }

    public static void logSystemInformation() {
        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put("Available Processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        dataMap.put("Available Memory", String.format("%s MiB", Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        dataMap.put("Available Storage", String.format("%s MiB", Arrays.stream(File.listRoots()).findFirst().orElseThrow().getUsableSpace() / (1024 * 1024)));
        dataMap.put("Operating System", String.format("%s %s %s", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")));
        dataMap.put("Java Vendor Version", System.getProperty("java.vendor.version"));

        printFormattedLog(AnsiColor.BRIGHT_BLUE, "System Information", dataMap);
    }

    public static void logApplicationInformation() {
        String hostName = null;
        String hostAddress = null;
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {}

        int serverPort = ApplicationUtil.getApplicationContext() instanceof ServletWebServerApplicationContext applicationContext ? applicationContext.getWebServer().getPort() : 0;
        Environment env = ApplicationUtil.getApplicationContext().getEnvironment();
        String protocol = env.getProperty("server.ssl.key-store") == null ? "http" : "https";

        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put("Name", env.getProperty("spring.application.name"));
        dataMap.put("Profiles", Arrays.toString(env.getActiveProfiles().length > 0 ? env.getActiveProfiles() : env.getDefaultProfiles()));
        dataMap.put("Hostname", hostName);
        dataMap.put("Local", String.format("%s://%s:%s", protocol, "localhost", serverPort));
        dataMap.put("External", String.format("%s://%s:%s", protocol, hostAddress, serverPort));

        try {
            dataMap.put("Database", SpringUtil.getBean(HikariDataSource.class).getJdbcUrl());
        } catch (Exception e) {
            try {
                Map<String, HikariDataSource> dataSources = SpringUtil.getBeans(HikariDataSource.class);
                for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
                    dataMap.put(entry.getKey(), entry.getValue().getJdbcUrl());
                }
            } catch (Exception ex) {}
        }

        printFormattedLog(AnsiColor.BRIGHT_GREEN, "Application Information", dataMap);
    }

    public static void logVersionInformation() {
        String javaVersion = System.getProperty("java.version");
        String springBootVersion = SpringBootVersion.getVersion();
        String hibernateVersion = org.hibernate.Version.getVersionString();
        String hazelcastVersion = HazelcastUtil.getInstance().getCluster().getLocalMember().getVersion().toString();

        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put("Java", "v" + javaVersion);
        dataMap.put("Spring Boot", "v" + springBootVersion);
        dataMap.put("Hibernate", "v" + hibernateVersion);
        dataMap.put("Hazelcast", "v" + hazelcastVersion);

        printFormattedLog(AnsiColor.BRIGHT_MAGENTA, "Version Information", dataMap);
    }

    private static void printFormattedLog(AnsiColor ansiColor, String title, Map<String, String> dataMap) {
        dataMap.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);

        int keyLength = dataMap.keySet().stream().filter(Objects::nonNull).mapToInt(String::length).max().orElse(0) + 1;
        int valueLength = dataMap.values().stream().filter(Objects::nonNull).mapToInt(String::length).max().orElse(0);
        String lineFormat = String.format("| %%-%ds: %%-%ds |\n", keyLength, valueLength);

        int totalLength = keyLength + valueLength + 2;
        String lineSeparator = String.format("+%s+\n", "-".repeat(totalLength + 2));

        int titleLength = totalLength - title.length();
        String titleFormat = String.format("| %s%%s%s |\n", " ".repeat(titleLength / 2), " ".repeat(titleLength / 2 + titleLength % 2));

        System.out.printf(lineSeparator);
        System.out.printf(titleFormat, AnsiOutput.toString(ansiColor, title));
        System.out.printf(lineSeparator);
        dataMap.forEach((key, value) -> System.out.printf(lineFormat, key, value));
        System.out.printf(lineSeparator);
    }
}
