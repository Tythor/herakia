package tythor.herakia.utility;

import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;

public class RedisUtil {
    @Getter
    private static RedissonClient client;
    @Getter
    private static CommandAsyncExecutor commandAsyncExecutor;

    public static RedissonClient createClient(Config config) {
        client = Redisson.create(config);
        commandAsyncExecutor = ((Redisson) client).getCommandExecutor();
        return client;
    }
}
