package tythor.herakia.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
class JacksonConfiguration {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(JsonMapper.Builder jsonMapperBuilder) {
        return jsonMapperBuilder.build();
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return jsonMapperBuilder -> {
            // Use reflection to serialize instead of getters/setters
            jsonMapperBuilder.changeDefaultVisibility(vc ->
                vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                    .withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY));

            jsonMapperBuilder.enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);

            // Disable features that should be disabled
            jsonMapperBuilder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            jsonMapperBuilder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        };
    }

    /**
     * Update when {@link org.hibernate.type.format.jackson.JacksonJsonFormatMapper JacksonJsonFormatMapper} supports Jackson 3.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(ObjectMapper objectMapper) {
        return hibernateProperties -> {
            hibernateProperties.put(AvailableSettings.JSON_FORMAT_MAPPER, new Jackson3JsonFormatMapper(objectMapper));
        };
    }
}
