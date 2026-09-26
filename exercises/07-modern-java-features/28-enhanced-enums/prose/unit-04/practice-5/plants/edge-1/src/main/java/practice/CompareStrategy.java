package practice;

import java.util.Comparator;
import java.util.List;

public enum CompareStrategy {
    NATURAL {
        @Override
        @SuppressWarnings("unchecked")
        public <T> int compare(T a, T b) {
            if (!(a instanceof Comparable<?>) || !(b instanceof Comparable<?>)) {
                throw new UnsupportedOperationException("Not comparable");
            }
            return ((Comparable<T>) a).compareTo(b);
        }
    },
    REVERSE {
        @Override
        @SuppressWarnings("unchecked")
        public <T> int compare(T a, T b) {
            if (!(a instanceof Comparable<?>) || !(b instanceof Comparable<?>)) {
                throw new UnsupportedOperationException("Not comparable");
            }
            return ((Comparable<T>) b).compareTo(a);
        }
    },
    BY_STRING {
        @Override
        public <T> int compare(T a, T b) {
            return NATURAL.compare(a, b);
        }
    };

    /** Compares {@code a} with {@code b} by this strategy. */
    public abstract <T> int compare(T a, T b);

    /** This strategy as a Comparator. */
    public <T> Comparator<T> toComparator() {
        return this::compare;
    }

    /** The least item by this strategy. */
    public <T> T min(List<T> items) {
        return items.stream().min(this::compare).orElseThrow();
    }

    /** The greatest item by this strategy. */
    public <T> T max(List<T> items) {
        return items.stream().max(this::compare).orElseThrow();
    }
}
