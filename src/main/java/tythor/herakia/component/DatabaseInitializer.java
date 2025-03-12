package tythor.herakia.component;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource) {
            initializeDatabase((HikariDataSource) bean);
        }
        return bean;
    }

    public void initializeDatabase(HikariDataSource hikariDataSource) {
        String username = hikariDataSource.getUsername();
        String password = hikariDataSource.getPassword();
        String jdbcUrl = hikariDataSource.getJdbcUrl();
        String databaseName = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1);

        jdbcUrl = jdbcUrl.replace(databaseName, "postgres");

        String existQuery = String.format("SELECT COUNT(*) FROM pg_database WHERE datname = '%s'", databaseName);
        String createQuery = String.format("CREATE DATABASE \"%s\"", databaseName);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(existQuery);
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                log.warn("Configured database=\"{}\" does not exist! Creating database now...", databaseName);
                statement.execute(createQuery);
            }
        } catch (SQLException e) {}
    }
}
