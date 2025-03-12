package tythor.herakia.hazelcast.management.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tythor.herakia.utility.HazelcastUtil;

@SuppressWarnings("removal")
@RestControllerEndpoint(id = "hazelcast")
@RequiredArgsConstructor
public class HzReverseProxy {
    private final RestTemplate restTemplate;

    @RequestMapping(value = "/**")
    public ResponseEntity<String> proxyRequest(RequestEntity<String> requestEntity) {
        int port = HazelcastUtil.getInstance().getCluster().getLocalMember().getAddress().getPort();
        String path = "/" + requestEntity.getUrl().getPath().split("/", 3)[2];
        String query = requestEntity.getUrl().getQuery() == null ? "" : "?" + requestEntity.getUrl().getQuery();
        String hazelcastUrl = "http://localhost:" + port + path + query;

        try {
            return restTemplate.exchange(hazelcastUrl, requestEntity.getMethod(), requestEntity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
        }
    }
}
