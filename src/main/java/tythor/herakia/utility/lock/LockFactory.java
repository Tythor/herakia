package tythor.herakia.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import tythor.herakia.component.AdvisoryLockManager;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.SpringUtil;

public abstract class LockFactory {
    public static LockProvider.Hazelcast HAZELCAST;
    public static LockProvider.Redis REDIS;
    public static LockProvider.Postgres POSTGRES;

    protected enum LockProvider {
        ;
        public enum Hazelcast {
            ;
        }
        public enum Redis {
            ;
        }
        public enum Postgres {
            ;
        }
    }

    public static HazelcastLockBuilder.LockMapStep builder() {
        // Keep # of method calls the same to preserve signature
        HazelcastInstance hazelcastInstance = HazelcastUtil.getInstance();
        return new HazelcastLockBuilder(hazelcastInstance).new LockMapStep();
    }

    public static HazelcastLockBuilder.LockMapStep builder(LockProvider.Hazelcast lockProvider) {
        HazelcastInstance hazelcastInstance = HazelcastUtil.getInstance();
        return new HazelcastLockBuilder(hazelcastInstance).new LockMapStep();
    }

    public static AbstractLockBuilder.LockKeyStep builder(LockProvider.Postgres lockProvider) {
        AdvisoryLockManager advisoryLockManager = SpringUtil.getBean(AdvisoryLockManager.class);
        return new PostgresLockBuilder(advisoryLockManager).new LockKeyStep();
    }

    /*public static RedisLockBuilder builder(LockProvider.Redis lockProvider) {
        return new RedisLockBuilder();
    }*/
}
