package practice;

import java.util.List;
import java.util.NoSuchElementException;

public enum Aggregator {
    SUM {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            throw new UnsupportedOperationException("write aggregate");
        }
    },
    AVERAGE {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            throw new UnsupportedOperationException("write aggregate");
        }
    },
    MIN {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            throw new UnsupportedOperationException("write aggregate");
        }
    },
    MAX {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            throw new UnsupportedOperationException("write aggregate");
        }
    },
    COUNT {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            throw new UnsupportedOperationException("write aggregate");
        }
    };

    /** Aggregates {@code values} into one number. */
    public abstract <T extends Number> double aggregate(List<T> values);
}
