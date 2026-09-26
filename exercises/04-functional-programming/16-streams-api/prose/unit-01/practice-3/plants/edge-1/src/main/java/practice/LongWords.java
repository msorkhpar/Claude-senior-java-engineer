package practice;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class LongWords {

    private LongWords() {
    }

    /** How many words are longer than the minimum, and which. */
    public record Report(long count, List<String> words) {
    }

    /** Returns a supplier whose every get() streams the words longer than {@code min}. */
    public static Supplier<Stream<String>> longerThan(List<String> words, int min) {
        Stream<String> longWords = words.stream().filter(word -> word.length() > min);
        return () -> longWords;
    }

    /** Returns the count and the list of the words longer than {@code min}. */
    public static Report report(List<String> words, int min) {
        List<String> longWords = longerThan(words, min).get().toList();
        return new Report(longWords.size(), longWords);
    }
}
