package practice;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class NullSafe {

    private NullSafe() {
    }

    /** A person whose nickname may be null. */
    public record Person(String name, String nickname) {
    }

    /** Returns the non-null nicknames, in the order of the people. */
    public static List<String> nicknames(List<Person> people) {
        return people.stream()
                .flatMap(person -> Stream.ofNullable(person.nickname()))
                .toList();
    }

    /** Returns how many items the array holds; a null array holds none. */
    public static long countItems(String[] items) {
        return Arrays.stream(items).count();
    }
}
