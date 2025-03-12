package tythor.herakia.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import tythor.herakia.utility.SpringUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Overrides {@link org.springframework.boot.hibernate.autoconfigure.HibernateJpaConfiguration HibernateJpaConfiguration}
 */
@Slf4j
public abstract class AbstractHibernateJpaConfig {
    protected abstract String getModule();
    protected abstract String getQualifier();
    protected abstract String getBasePackage();
    protected abstract String getBeanPrefix();

    private JpaProperties jpaProperties() {
        return bindProperties("spring.jpa", new JpaProperties());
    }

    private HibernateProperties hibernateProperties() {
        return bindProperties("spring.jpa.hibernate", new HibernateProperties());
    }

    private HikariConfig hikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(getBeanPrefix() + "HikariPool");
        return bindProperties("spring.datasource.hikari", hikariConfig);
    }

    public HikariDataSource hikariDataSource() {
        return new HikariDataSource(hikariConfig());
    }

    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(hikariDataSource());
    }

    public AbstractEntityManagerFactoryBean entityManagerFactory() {
        HibernateSettings hibernateSettings = new HibernateSettings().hibernatePropertiesCustomizers(SpringUtil.getBeans(HibernatePropertiesCustomizer.class).values());
        Map<String, Object> properties = hibernateProperties().determineHibernateProperties(jpaProperties().getProperties(), hibernateSettings);
        PersistenceManagedTypes managedTypes = createPersistenceManagedTypes(getBasePackage(), getQualifier());

        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), (dataSource) -> properties, null)
            .dataSource(hikariDataSource())
            .properties(properties)
            .managedTypes(managedTypes)
            .build();
    }

    public PlatformTransactionManager transactionManager() {
//        String entityManagerFactoryName = "entityManagerFactory_" + getModule() + "_" + getDatabaseQualifier();
//        EntityManagerFactory entityManagerFactory = SpringUtil.getBean(entityManagerFactoryName, EntityManagerFactory.class);
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    private <T> T bindProperties(String configurationProperties, T bean) {
        Bindable<?> bindable = Bindable.ofInstance(bean);
        Binder binder = Binder.get(SpringUtil.getBean(Environment.class));
        binder.bind(configurationProperties, bindable);
        binder.bind("%s.%s.%s".formatted(getModule(), getQualifier(), configurationProperties), bindable);
        return bean;
    }

    private PersistenceManagedTypes createPersistenceManagedTypes(String basePackage, String regexFilter) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

        // Only include classes annotated with @Entity
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        // Add a regex filter for classes containing regexFilter in the fully qualified name
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*" + regexFilter + ".*")));

        List<BeanDefinition> beanDefinitions = provider.findCandidateComponents(basePackage).stream().toList();

        List<String> classes = new ArrayList<>();
        List<String> packages = new ArrayList<>();

        // Add BeanDefinitions to classes and packages
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                classes.add(beanClass.getName());
                packages.add(beanClass.getPackage().getName());
            } catch (Exception e) {}
        }

        return new SimplePersistenceManagedTypes(classes, packages, null);
    }

    @Data
    @AllArgsConstructor
    private class SimplePersistenceManagedTypes implements PersistenceManagedTypes {
        private final List<String> managedClassNames;
        private final List<String> managedPackages;
        private final URL persistenceUnitRootUrl;
    }
}
