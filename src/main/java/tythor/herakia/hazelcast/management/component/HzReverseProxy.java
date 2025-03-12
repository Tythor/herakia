package tythor.herakia.hazelcast.management.component;

import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tythor.herakia.utility.HazelcastUtil;

import java.util.Objects;

@RestController
@RequestMapping(path = "/hazelcast")
@RequiredArgsConstructor
public class HzReverseProxy {
    private final RestTemplate restTemplate;

    @RequestMapping(value = "/**")
    public ResponseEntity<String> proxyRequest(RequestEntity<String> requestEntity) {
        int port = HazelcastUtil.getInstance().getCluster().getLocalMember().getAddress().getPort();
        String hazelcastUrl = "http://localhost:" + port + requestEntity.getUrl().getPath() + (requestEntity.getUrl().getQuery() == null ? "" : "?" + requestEntity.getUrl().getQuery());

        try {
            return restTemplate.exchange(hazelcastUrl, Objects.requireNonNull(requestEntity.getMethod()), requestEntity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
        }
    }
}
