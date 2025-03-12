/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package tythor.herakia.configuration;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.AbstractJsonFormatMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Adapted {@link org.hibernate.type.format.jackson.JacksonJsonFormatMapper JacksonJsonFormatMapper} to Jackson 3.
 */
public final class Jackson3JsonFormatMapper extends AbstractJsonFormatMapper {

    private final ObjectMapper objectMapper;

    public Jackson3JsonFormatMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void writeToTarget(T value, JavaType<T> javaType, Object target, WrapperOptions options) throws IOException {
        objectMapper.writerFor(objectMapper.constructType(javaType.getJavaType())).writeValue((JsonGenerator) target, value);
    }

    @Override
    public <T> T readFromSource(JavaType<T> javaType, Object source, WrapperOptions options) throws IOException {
        return objectMapper.readValue((JsonParser) source, objectMapper.constructType(javaType.getJavaType()));
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return JsonParser.class.isAssignableFrom(sourceType);
    }

    @Override
    public boolean supportsTargetType(Class<?> targetType) {
        return JsonGenerator.class.isAssignableFrom(targetType);
    }

    @Override
    public <T> T fromString(CharSequence charSequence, Type type) {
        try {
            return objectMapper.readValue(charSequence.toString(), objectMapper.constructType(type));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not deserialize string to java type: " + type, e);
        }
    }

    @Override
    public <T> String toString(T value, Type type) {
        try {
            return objectMapper.writerFor(objectMapper.constructType(type)).writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not serialize object of java type: " + type, e);
        }
    }

}
