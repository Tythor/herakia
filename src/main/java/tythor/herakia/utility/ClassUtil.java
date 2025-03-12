package tythor.herakia.utility;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ClassUtil {
    public static List<Class<?>> getAllClassesInPackage(String packageName) {
        // Find classes in given package
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(packageName);

        List<Class<?>> classes = new ArrayList<>();
        // Convert BeanDefinition to Class
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                classes.add(beanClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return classes;
    }

    public static List<Class<?>> getAllClassesInBasePackage() {
        return getAllClassesInPackage(AutoConfigurationPackages.get(ApplicationUtil.getApplicationContext()).getFirst());
    }
}
