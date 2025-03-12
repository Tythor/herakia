package tythor.herakia.configuration;

/**
 * A custom Postgres dialect to avoid check constraints for enums in Hibernate 6.2+.
 * <p>
 * <a href="https://docs.jboss.org/hibernate/orm/6.2/introduction/html_single/Hibernate_Introduction.html#enums">https://docs.jboss.org/hibernate/orm/6.2/introduction/html_single/Hibernate_Introduction.html#enums</a>
 */
public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {
    @Override
    public String getCheckCondition(String columnName, String[] values) {
        return null;
    }

    @Override
    public String getCheckCondition(String columnName, Long[] values) {
        return null;
    }
}
