package practice;

import java.util.List;

public final class Expand {

    private Expand() {
    }

    /** Emits each number followed by its square. */
    public static List<Integer> valueAndSquare(List<Integer> numbers) {
        return numbers.stream()
                .<Integer>mapMulti((n, downstream) -> {
                    downstream.accept(n);
                    downstream.accept(n * n);
                })
                .toList();
    }

    /** Emits each String element upper-cased, and nothing for any other element. */
    public static List<String> upperStrings(List<Object> mixed) {
        return mixed.stream()
                .<String>mapMulti((element, downstream) -> {
                    if (element.getClass() == String.class) {
                        downstream.accept(((String) element).toUpperCase());
                    }
                })
                .toList();
    }
}
