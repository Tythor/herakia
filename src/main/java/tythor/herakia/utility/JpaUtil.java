package tythor.herakia.utility;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Arrays;

public class JpaUtil {
    public static String getTableName(EntityManagerFactory entityManagerFactory, Class<?> entityClass) {
        EntityPersister entityPersister = getEntityPersister(entityManagerFactory, entityClass);
        return entityPersister.getEntityName();
    }

    public static String getColumnName(EntityManagerFactory entityManagerFactory, Class<?> entityClass, String propertyName) {
        AbstractEntityPersister abstractEntityPersister = (AbstractEntityPersister) getEntityPersister(entityManagerFactory, entityClass);
        return Arrays.stream(abstractEntityPersister.toColumns(propertyName)).findAny().orElse(null);
    }

    public static EntityPersister getEntityPersister(EntityManagerFactory entityManagerFactory, Class<?> entityClass) {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        MappingMetamodel metamodel = sessionFactory.getRuntimeMetamodels().getMappingMetamodel();
        return metamodel.getEntityDescriptor(entityClass);
    }
}
