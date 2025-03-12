package tythor.herakia.annotation.cacherefresh;

import com.hazelcast.map.IMap;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import tythor.herakia.configuration.HerakiaHazelcastConfig;
import tythor.herakia.exception.LockedException;
import tythor.herakia.utility.ClassUtil;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.SpringUtil;
import tythor.herakia.utility.thread.VirtualThreadUtil;
import tythor.herakia.utility.lock.LockFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

/**
 * An annotation processor for {@link CacheRefresh @CacheRefresh} backed by Hazelcast.
 * <p>
 * <b>Note:</b> {@link org.springframework.cache.Cache#getNativeCache()} must be an implementation of {@link com.hazelcast.map.IMap IMap}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshProcessor {
    @Lazy
    private final CacheManager cacheManager;
    private final TaskScheduler taskScheduler;
    private final Environment environment;

    @PostConstruct
    public void init() {
        List<Method> cacheRefreshMethods = ClassUtil.getAllClassesInBasePackage().stream()
            .flatMap(beanClass -> Arrays.stream(beanClass.getMethods()))
            .filter(method -> method.isAnnotationPresent(CacheRefresh.class))
            .toList();

        for (Method method : cacheRefreshMethods) {
            method.setAccessible(true);
            CacheRefresh cacheRefresh = method.getAnnotation(CacheRefresh.class);

            String[] profiles = cacheRefresh.profiles();
            if (profiles.length > 0 && environment.matchesProfiles(profiles)) {
                CronTrigger cronTrigger = cacheRefresh.zone().isBlank()
                    ? new CronTrigger(cacheRefresh.cron())
                    : new CronTrigger(cacheRefresh.cron(), TimeZone.getTimeZone(cacheRefresh.zone()));
                taskScheduler.schedule(() -> executeScheduledTask(method, cacheRefresh), cronTrigger);
            }
        }
    }

    private void executeScheduledTask(Method method, CacheRefresh cacheRefresh) {
        IMap<Object, Object> cacheRefreshMap = HazelcastUtil.getMap(HerakiaHazelcastConfig.CACHE_REFRESH_MAP);
        try {
            LockFactory.builder().lockMap(cacheRefreshMap).lockKey(method.toString()).tryLock()
                .task(() -> executeCacheRefreshTask(method, cacheRefresh)).execute();
        } catch (LockedException e) {}
    }

    private void executeCacheRefreshTask(Method method, CacheRefresh cacheRefresh) {
        try (ExecutorService executorService = VirtualThreadUtil.getNewVirtualExecutor()) {
            for (String cacheName : cacheRefresh.value()) {
                executorService.execute(() -> refreshCacheByKeys(method, cacheName));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshCacheByKeys(Method method, String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        switch (cache.getNativeCache()) {
            case IMap<?, ?> iMap -> refreshMapByKeys(method, (IMap<Object, Object>) iMap);
            default -> {}
        }
    }

    private void refreshMapByKeys(Method method, IMap<Object, Object> map) {
        for (Object key : map.keySet()) {
            Object bean = SpringUtil.getBeanWithoutProxy(method.getDeclaringClass());
            Object[] args = inverseGenerateKey(key);
            try {
                Object result = method.invoke(bean, args);
                if (result != null) map.set(key, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Does the inverse of {@link org.springframework.cache.interceptor.SimpleKeyGenerator#generateKey(Object...)}
     */
    @SneakyThrows
    private Object[] inverseGenerateKey(Object key) {
        if (key instanceof SimpleKey simpleKey) {
            if (simpleKey.equals(SimpleKey.EMPTY)) {
                return new Object[]{};
            } else {
                // Using reflection since SimpleKey.params is not a publicly accessible field
                Field field = SimpleKey.class.getDeclaredField("params");
                field.setAccessible(true);
                return (Object[]) field.get(simpleKey);
            }
        } else {
            return new Object[]{key};
        }
    }
}
