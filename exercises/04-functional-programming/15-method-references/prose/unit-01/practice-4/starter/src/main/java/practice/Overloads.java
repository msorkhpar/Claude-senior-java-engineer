package practice;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

public final class Overloads {

    private Overloads() {
    }

    /** Removes the given value from the list. */
    public static BiConsumer<List<Integer>, Integer> removeValue() {
        throw new UnsupportedOperationException("write removeValue");
    }

    /** Removes the element at the given index. */
    public static ObjIntConsumer<List<Integer>> removeAt() {
        throw new UnsupportedOperationException("write removeAt");
    }

    /** Turns a character array into the string it spells. */
    public static Function<char[], String> text() {
        throw new UnsupportedOperationException("write text");
    }
}
