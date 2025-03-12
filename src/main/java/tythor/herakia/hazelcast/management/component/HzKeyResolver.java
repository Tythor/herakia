package tythor.herakia.hazelcast.management.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class HzKeyResolver {
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public <T> T resolve(String keyString, Class<T> keyClass) {
        // ObjectMapper cannot parse String into String
        if (String.class.isAssignableFrom(keyClass)) return (T) keyString;

        try {
            return objectMapper.readValue(keyString, keyClass);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not parse raw key: " + keyString, e);
        }
    }
}
