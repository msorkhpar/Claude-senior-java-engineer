package practice;

import java.util.List;
import java.util.NoSuchElementException;

public enum Aggregator {
    SUM {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.stream().mapToDouble(Number::doubleValue).sum();
        }
    },
    AVERAGE {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
        }
    },
    MIN {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.stream().mapToDouble(Number::doubleValue).min()
                    .orElseThrow(() -> new NoSuchElementException("Empty list"));
        }
    },
    MAX {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.stream().mapToDouble(Number::doubleValue).max()
                    .orElseThrow(() -> new NoSuchElementException("Empty list"));
        }
    },
    COUNT {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.size();
        }
    };

    /** Aggregates {@code values} into one number. */
    public abstract <T extends Number> double aggregate(List<T> values);
}
