package tythor.herakia.utility;

import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ClassUtil {
    public static List<Class<?>> getAllClassesFromProvider(String packageName, ClassPathScanningCandidateComponentProvider provider) {
        // Convert BeanDefinition to Class
        return provider.findCandidateComponents(packageName).stream()
            .map(x -> {
                try {
                    return Class.forName(x.getBeanClassName());
                } catch (Throwable t) {
                    return (Class<?>) null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public static List<Class<?>> getAllClassesInPackage(String packageName) {
        // Find classes in given package
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        return getAllClassesFromProvider(packageName, provider);
    }

    public static List<Class<?>> getAllClassesInBasePackage() {
        return getAllClassesInPackage(AutoConfigurationPackages.get(ApplicationUtil.getApplicationContext()).getFirst());
    }
}
