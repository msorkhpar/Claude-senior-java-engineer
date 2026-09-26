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
        return List::remove;
    }

    /** Removes the element at the given index. */
    public static ObjIntConsumer<List<Integer>> removeAt() {
        return List::remove;
    }

    /** Turns a character array into the string it spells. */
    public static Function<char[], String> text() {
        return String::valueOf;
    }
}
