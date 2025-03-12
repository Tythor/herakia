package tythor.herakia.utility;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

public class JacksonUtil {
    public static ObjectMapper getNonReflectionObjectMapper() {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

        // Fix is_ prefix for boolean fields
        objectMapper.setPropertyNamingStrategy(new BooleanPropertyNamingStrategy());

        // Use getters/setters to serialize instead of reflection
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.DEFAULT);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.DEFAULT);

        return objectMapper;
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

