package practice;

import java.util.Comparator;
import java.util.List;

public enum CompareStrategy {
    NATURAL {
        @Override
        @SuppressWarnings("unchecked")
        public <T> int compare(T a, T b) {
            throw new IllegalStateException("write compare");
        }
    },
    REVERSE {
        @Override
        @SuppressWarnings("unchecked")
        public <T> int compare(T a, T b) {
            throw new IllegalStateException("write compare");
        }
    },
    BY_STRING {
        @Override
        public <T> int compare(T a, T b) {
            throw new IllegalStateException("write compare");
        }
    };

    /** Compares {@code a} with {@code b} by this strategy. */
    public abstract <T> int compare(T a, T b);

    /** This strategy as a Comparator. */
    public <T> Comparator<T> toComparator() {
        throw new IllegalStateException("write toComparator");
    }

    /** The least item by this strategy. */
    public <T> T min(List<T> items) {
        throw new IllegalStateException("write min");
    }

    /** The greatest item by this strategy. */
    public <T> T max(List<T> items) {
        throw new IllegalStateException("write max");
    }
}
