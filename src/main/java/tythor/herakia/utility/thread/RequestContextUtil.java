package tythor.herakia.utility.thread;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class RequestContextUtil {
    public static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static <T> Optional<T> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        return (T) requestAttributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST);
    }

    public static void set(String key, Object value) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        requestAttributes.setAttribute(key, value, RequestAttributes.SCOPE_REQUEST);
    }
}
