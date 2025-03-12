package tythor.herakia.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DatabaseInitializer {
    public static void initializeDatabases(ConfigurableEnvironment environment) {
        for (Map<String, String> properties : getGroupedPropertiesMap(environment).values()) {
            String username = properties.get("spring.datasource.hikari.username");
            String password = properties.get("spring.datasource.hikari.password");
            String jdbcUrl = properties.get("spring.datasource.hikari.jdbc-url");

            if (username == null || password == null || jdbcUrl == null) continue;

            String databaseName = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1);
            String postgresJdbcUrl = jdbcUrl.replace(databaseName, "postgres");

            String existQuery = String.format("SELECT COUNT(*) FROM pg_database WHERE datname = '%s'", databaseName);
            String createQuery = String.format("CREATE DATABASE \"%s\"", databaseName);

            try (Connection connection = DriverManager.getConnection(postgresJdbcUrl, username, password);
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(existQuery);
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    log.warn("Database at jdbcUrl=\"{}\" does not exist! Creating database now...", jdbcUrl);
                    statement.execute(createQuery);
                }
            } catch (SQLException e) {}
        }
    }

    private static Map<String, Map<String, String>> getGroupedPropertiesMap(ConfigurableEnvironment environment) {
        Map<String, Map<String, String>> groupedPropertiesMap = new HashMap<>();

        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?> source) {
                for (String propertyName : source.getPropertyNames()) {
                    if (!propertyName.contains("spring.datasource.hikari.")) continue;

                    String[] propertySplit = propertyName.split("\\.");
                    String propertyGroup = propertySplit[0] + "." + propertySplit[1];

                    String propertyKey = String.join(".", Arrays.copyOfRange(propertySplit, 2, propertySplit.length));
                    String propertyValue = environment.getProperty(propertyName);

                    Map<String, String> propertiesMap = groupedPropertiesMap.computeIfAbsent(propertyGroup, k -> new HashMap<>());
                    propertiesMap.put(propertyKey, propertyValue);
                }
            }
        }

        return groupedPropertiesMap;
    }
}
