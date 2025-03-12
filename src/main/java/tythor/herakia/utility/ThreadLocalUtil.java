package tythor.herakia.utility;

import java.util.function.Supplier;

public class ThreadLocalUtil {
    public static <T> InheritableThreadLocal<T> withInitial(Supplier<? extends T> supplier) {
        return new InheritableThreadLocal<>() {
            @Override
            protected T initialValue() {
                return supplier.get();
            }
        };
    }
}
