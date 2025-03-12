package tythor.herakia.hazelcast.management.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HzKeyResolver {
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public <T> T resolve(String keyString, Class<T> keyClass) {
        // ObjectMapper cannot parse String into String
        if (keyClass.isAssignableFrom(String.class)) return (T) keyString;

        try {
            return objectMapper.readValue(keyString, keyClass);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not parse raw key: " + keyString, e);
        }
    }
}
