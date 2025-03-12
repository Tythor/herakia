package tythor.herakia.utility.thread;

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
