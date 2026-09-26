package practice;

import java.util.function.Supplier;

public class Lazy<T> {
    private final T value;

    public Lazy(Supplier<T> supplier) {
        this.value = supplier.get();
    }

    public T get() {
        return value;
    }
}
