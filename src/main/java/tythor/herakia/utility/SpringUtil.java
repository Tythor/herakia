package tythor.herakia.utility;

import org.springframework.aop.framework.AopProxyUtils;

import java.util.Map;

public class SpringUtil {
    public static <T> T getBean(Class<T> requiredType) {
        return ApplicationUtil.getApplicationContext().getBean(requiredType);
    }

    public static <T> T getBean(String beanName, Class<T> requiredType) {
        return ApplicationUtil.getApplicationContext().getBean(beanName, requiredType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBeanWithoutProxy(Class<T> requiredType) {
        return (T) AopProxyUtils.getSingletonTarget(getBean(requiredType));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBeanWithoutProxy(String beanName, Class<T> requiredType) {
        return (T) AopProxyUtils.getSingletonTarget(getBean(beanName, requiredType));
    }

    public static <T> Map<String, T> getBeans(Class<T> requiredType) {
        return ApplicationUtil.getApplicationContext().getBeansOfType(requiredType);
    }
}
