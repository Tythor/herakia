package tythor.herakia.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class HerakiaHazelcastConfig {
    public static final String LOCK_MAP = "lock_map";
    public static final String CACHE_REFRESH_MAP = "cache_refresh_map";
}
