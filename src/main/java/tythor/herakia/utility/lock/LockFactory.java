package tythor.herakia.utility.lock;

public abstract class LockFactory {
    public static LockProvider.Hazelcast HAZELCAST;
    public static LockProvider.Postgres REDIS;
    public static LockProvider.Postgres POSTGRES;


    protected enum LockProvider {
        ;
        public enum Hazelcast {
            ;
        }

        public enum Postgres {
            ;
        }
    }

    public static HazelcastLockBuilder.LockMapStep builder() {
        return builder(HAZELCAST);
    }

    public static HazelcastLockBuilder.LockMapStep builder(LockProvider.Hazelcast lockProvider) {
        return new HazelcastLockBuilder().new LockMapStep();
    }

    public static AbstractLockBuilder.LockKeyStep builder(LockProvider.Postgres lockProvider) {
        return new PostgresLockBuilder().new LockKeyStep();
    }

    /*public static RedisLockBuilder builder(LockProvider.Redis lockProvider) {
        return new RedisLockBuilder();
    }*/
}
