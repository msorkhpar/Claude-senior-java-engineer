package practice;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/** A processing pipeline whose skeleton is written once: keep the valid items, then transform them. */
public abstract class DataProcessor<T, R> {

    /** The template method: the one pipeline every processor shares. */
    public final List<R> process(List<T> items) {
        Objects.requireNonNull(items, "items must not be null");
        return items.stream()
                .filter(this::isValid)
                .map(this::transform)
                .collect(Collectors.toList());
    }

    /** Whether this item takes part. */
    protected abstract boolean isValid(T item);

    /** What this item becomes. */
    protected abstract R transform(T item);

    /** Keeps the strings that are not null or blank, in upper case. */
    public static final class UpperCase extends DataProcessor<String, String> {

        @Override
        protected boolean isValid(String item) {
            return item != null && !item.isBlank();
        }

        @Override
        protected String transform(String item) {
            return item.toUpperCase();
        }
    }

    /** Keeps the positive integers, doubled. */
    public static final class Doubler extends DataProcessor<Integer, Integer> {

        @Override
        protected boolean isValid(Integer item) {
            return item != null && item > 0;
        }

        @Override
        protected Integer transform(Integer item) {
            return item * 2;
        }
    }
}
