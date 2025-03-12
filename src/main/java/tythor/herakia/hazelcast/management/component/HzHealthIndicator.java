package tythor.herakia.hazelcast.management.component;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.instance.impl.NodeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tythor.herakia.utility.HazelcastUtil;

import java.util.Map;

@Slf4j
@Component("hzHealthIndicator") // Controls the indicator name
@RequiredArgsConstructor
public class HzHealthIndicator extends AbstractHealthIndicator {
    private final RestTemplate restTemplate;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (HazelcastUtil.getInstance() == null) return;

        int port = HazelcastUtil.getInstance().getCluster().getLocalMember().getAddress().getPort();
        String hazelcastUrl = "http://localhost:" + port + "/hazelcast/health";

        try {
            ResponseEntity<Map<String, ?>> responseEntity = restTemplate.exchange(hazelcastUrl, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {});
            Map<String, ?> responseBody = responseEntity.getBody();

            if (evaluateHealth(responseBody)) {
                builder.up().withDetails(responseBody);
            } else {
                builder.down().withDetails(responseBody);
            }
        } catch (HttpStatusCodeException e) {
            Map<String, ?> responseBody = e.getResponseBodyAs(new ParameterizedTypeReference<>() {});
            builder.down().withDetails(responseBody);
        }
    }

    private boolean evaluateHealth(Map<String, ?> responseBody) {
        boolean isNodeStateActive = responseBody.get("nodeState").equals(NodeState.ACTIVE.toString());
        boolean isClusterStateActive = responseBody.get("clusterState").equals(ClusterState.ACTIVE.toString());
        boolean isClusterSafe = responseBody.get("clusterSafe").equals(Boolean.TRUE);
        return isNodeStateActive && isClusterStateActive && isClusterSafe;
    }
}
