package tythor.herakia.hazelcast.management.dto;

import com.hazelcast.cluster.Member;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PingStats {
    private BigDecimal ping;
    private String memberString;
    private String hostname;
    private String instance;

    public PingStats(Member member, BigDecimal ping) {
        this.ping = ping;
        this.memberString = member.toString();
        this.hostname = member.getAttribute("hostname");
        this.instance = member.getAttribute("instance");
    }
}
