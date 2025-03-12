package tythor.herakia.utility;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestContextUtil {
    private static final InheritableThreadLocal<Map<Object, Object>> threadLocalMap = ThreadLocalUtil.withInitial(HashMap::new);

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(String key) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return Optional.ofNullable((T) requestAttributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST));
        } else {
            return Optional.ofNullable((T) threadLocalMap.get().get(key));
        }
    }

    public static void set(String key, Object value) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(key, value, RequestAttributes.SCOPE_REQUEST);
        } else {
            threadLocalMap.get().put(key, value);
        }
    }
}
