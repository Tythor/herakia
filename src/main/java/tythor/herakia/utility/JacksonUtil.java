package tythor.herakia.utility;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMethod;
import tools.jackson.databind.json.JsonMapper;

public class JacksonUtil {
    public static ObjectMapper getNonReflectionObjectMapper() {
        JsonMapper.Builder builder = SpringUtil.getBean(JsonMapper.Builder.class);

        // Fix is_ prefix for boolean fields
        builder.propertyNamingStrategy(new BooleanPropertyNamingStrategy());

        // Use getters/setters to serialize instead of reflection
        builder.changeDefaultVisibility(vc ->
            vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.DEFAULT)
                .withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.DEFAULT));

        return builder.build();
    }

    public static class BooleanPropertyNamingStrategy extends PropertyNamingStrategy {
        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            boolean hasOneParameter = method.getParameterCount() == 1;
            boolean isABoolean = method.getRawParameterType(0) == Boolean.class || method.getRawParameterType(0) == boolean.class;
            boolean startsWithSet = method.getName().startsWith("set");

            if (hasOneParameter && isABoolean && startsWithSet) {
                Class<?> containingClass = method.getDeclaringClass();
                String potentialFieldName = "is" + method.getName().substring(3);

                try {
                    containingClass.getDeclaredField(potentialFieldName);
                    return potentialFieldName;
                } catch (NoSuchFieldException e) {}
            }

            return super.nameForSetterMethod(config, method, defaultName);
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            boolean isABoolean = method.getRawReturnType() == Boolean.class || method.getRawReturnType() == boolean.class;
            boolean startsWithIs = method.getName().startsWith("is");

            if (isABoolean && startsWithIs) {
                Class<?> containingClass = method.getDeclaringClass();
                String potentialFieldName = method.getName();

                try {
                    containingClass.getDeclaredField(potentialFieldName);
                    return potentialFieldName;
                } catch (NoSuchFieldException e) {}
            }
            return super.nameForGetterMethod(config, method, defaultName);
        }
    }
}

