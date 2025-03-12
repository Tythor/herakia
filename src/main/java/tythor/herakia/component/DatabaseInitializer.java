package tythor.herakia.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DatabaseInitializer {
    public static void initializeDatabases(ConfigurableEnvironment environment) {
        for (Map<String, String> properties : getGroupedDataSourcePropertiesMap(environment).values()) {
            String username = properties.get(properties.keySet().stream().filter(x -> x.contains("username")).findAny().orElse(""));
            String password = properties.get(properties.keySet().stream().filter(x -> x.contains("password")).findAny().orElse(""));
            String url = properties.get(properties.keySet().stream().filter(x -> x.contains("url")).findAny().orElse(""));

            if (username == null || password == null || url == null) continue;

            String databaseName = url.substring(url.lastIndexOf('/') + 1);
            String postgresUrl = url.replace(databaseName, "postgres");

            String existQuery = String.format("SELECT COUNT(*) FROM pg_database WHERE datname = '%s'", databaseName);
            String createQuery = String.format("CREATE DATABASE \"%s\"", databaseName);

            try (Connection connection = DriverManager.getConnection(postgresUrl, username, password);
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(existQuery);
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    log.warn("Database at url=\"{}\" does not exist! Creating database now...", url);
                    statement.execute(createQuery);
                }
            } catch (SQLException e) {}
        }
    }

    private static Map<String, Map<String, String>> getGroupedDataSourcePropertiesMap(ConfigurableEnvironment environment) {
        Map<String, Map<String, String>> groupedDataSourcePropertiesMap = new HashMap<>();

        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?> source) {
                for (String propertyName : source.getPropertyNames()) {
                    if (!propertyName.contains("spring.datasource.")) continue;

                    String[] propertySplit = propertyName.split("\\.");
                    String propertyGroup = propertySplit[0] + "." + propertySplit[1];

                    String propertyKey = String.join(".", Arrays.copyOfRange(propertySplit, 2, propertySplit.length));
                    String propertyValue = environment.getProperty(propertyName);

                    Map<String, String> propertiesMap = groupedDataSourcePropertiesMap.computeIfAbsent(propertyGroup, k -> new HashMap<>());
                    propertiesMap.put(propertyKey, propertyValue);
                }
            }
        }

        return groupedDataSourcePropertiesMap;
    }
}
