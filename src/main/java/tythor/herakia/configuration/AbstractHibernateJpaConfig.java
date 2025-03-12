package tythor.herakia.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.Entity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import tythor.herakia.utility.ClassUtil;
import tythor.herakia.utility.SpringUtil;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Overrides {@link org.springframework.boot.hibernate.autoconfigure.HibernateJpaConfiguration HibernateJpaConfiguration}
 */
@Slf4j
public abstract class AbstractHibernateJpaConfig {
    protected abstract String getModule();
    protected abstract String getQualifier();
    protected abstract String getBasePackage();
    protected abstract String getBeanPrefix();

    protected Collection<String> additionalEntityClasses() {
        return List.of();
    }

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
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    private <T> T bindProperties(String configurationProperties, T bean) {
        Bindable<?> bindable = Bindable.ofInstance(bean);
        Binder binder = Binder.get(SpringUtil.getBean(Environment.class));
        binder.bind(configurationProperties, bindable);
        binder.bind("%s.%s.%s".formatted(getModule(), getQualifier(), configurationProperties), bindable);
        return bean;
    }

    private PersistenceManagedTypes createPersistenceManagedTypes(String packageName, String regexFilter) {
        List<TypeFilter> typeFilters = new ArrayList<>();
        // Only include classes annotated with @Entity
        typeFilters.add(new AnnotationTypeFilter(Entity.class));
        // Add a regex filter for classes containing regexFilter in the fully qualified name
        typeFilters.add(new RegexPatternTypeFilter(Pattern.compile(".*" + regexFilter + ".*")));

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter((metadataReader, factory) -> typeFilters.stream().allMatch(x -> match(metadataReader, factory, x)));

        List<String> classes = Stream.concat(
                ClassUtil.getAllClassesFromProvider(packageName, provider).stream().map(x -> x.getName()),
                additionalEntityClasses().stream()
            ).distinct().toList();

        return PersistenceManagedTypes.of(classes.toArray(String[]::new));
    }

    @SneakyThrows
    private static boolean match(MetadataReader metadataReader, MetadataReaderFactory factory, TypeFilter typeFilter) {
        return typeFilter.match(metadataReader, factory);
    }
}
