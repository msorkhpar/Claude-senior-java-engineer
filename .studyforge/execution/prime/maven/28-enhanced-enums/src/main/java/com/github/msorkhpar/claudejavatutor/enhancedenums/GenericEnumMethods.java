package com.github.msorkhpar.claudejavatutor.enhancedenums;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates generic methods within enums.
 * While enum types cannot be generic, their individual methods can declare type parameters.
 */
public class GenericEnumMethods {

    // --- Example 1: Enum with generic converter methods ---

    /**
     * Data type converter enum with generic conversion methods.
     */
    public enum DataConverter {
        STRING_TO_NUMBER {
            @Override
            public <T> T convert(Object input, Class<T> targetType) {
                String str = String.valueOf(input);
                Object result;
                if (targetType == Integer.class) {
                    result = Integer.parseInt(str);
                } else if (targetType == Long.class) {
                    result = Long.parseLong(str);
                } else if (targetType == Double.class) {
                    result = Double.parseDouble(str);
                } else if (targetType == Float.class) {
                    result = Float.parseFloat(str);
                } else {
                    throw new UnsupportedOperationException(
                            "Cannot convert to " + targetType.getSimpleName());
                }
                return targetType.cast(result);
            }
        },
        TO_STRING {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Object input, Class<T> targetType) {
                if (targetType != String.class) {
                    throw new UnsupportedOperationException(
                            "TO_STRING only converts to String");
                }
                return (T) String.valueOf(input);
            }
        },
        IDENTITY {
            @Override
            public <T> T convert(Object input, Class<T> targetType) {
                return targetType.cast(input);
            }
        };

        /**
         * Converts an input object to the specified target type.
         *
         * @param input      the input value to convert
         * @param targetType the class of the desired output type
         * @param <T>        the target type
         * @return the converted value
         */
        public abstract <T> T convert(Object input, Class<T> targetType);

        /**
         * Safely converts, returning Optional.empty() on failure.
         */
        public <T> Optional<T> safeConvert(Object input, Class<T> targetType) {
            try {
                return Optional.ofNullable(convert(input, targetType));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    // --- Example 2: Enum with generic collection operations ---

    /**
     * Collection operation enum demonstrating generic methods on collections.
     */
    public enum CollectionOp {
        FILTER {
            @Override
            public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
                return input.stream()
                        .filter(predicate)
                        .collect(Collectors.toList());
            }
        },
        SORT {
            @Override
            @SuppressWarnings("unchecked")
            public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
                // For SORT, predicate is ignored; natural ordering is used
                List<T> sorted = new ArrayList<>(input);
                if (!sorted.isEmpty() && sorted.getFirst() instanceof Comparable) {
                    sorted.sort((a, b) -> ((Comparable<T>) a).compareTo(b));
                }
                return sorted;
            }
        },
        DISTINCT {
            @Override
            public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
                // For DISTINCT, predicate is ignored
                return input.stream()
                        .distinct()
                        .collect(Collectors.toList());
            }
        },
        REVERSE {
            @Override
            public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
                // For REVERSE, predicate is ignored
                List<T> reversed = new ArrayList<>(input);
                Collections.reverse(reversed);
                return reversed;
            }
        };

        /**
         * Executes the collection operation.
         *
         * @param input     the input list
         * @param predicate a predicate (used by FILTER; ignored by others)
         * @param <T>       the element type
         * @return the resulting list
         */
        public abstract <T> List<T> execute(List<T> input, Predicate<T> predicate);

        /**
         * Convenience method for operations that don't need a predicate.
         */
        public <T> List<T> execute(List<T> input) {
            return execute(input, t -> true);
        }
    }

    // --- Example 3: Enum with generic transformation and mapping ---

    /**
     * Enum demonstrating generic map/transform/reduce methods.
     */
    public enum Aggregator {
        SUM {
            @Override
            public <T extends Number> double aggregate(List<T> values) {
                return values.stream()
                        .mapToDouble(Number::doubleValue)
                        .sum();
            }
        },
        AVERAGE {
            @Override
            public <T extends Number> double aggregate(List<T> values) {
                if (values.isEmpty()) {
                    return 0.0;
                }
                return values.stream()
                        .mapToDouble(Number::doubleValue)
                        .average()
                        .orElse(0.0);
            }
        },
        MIN {
            @Override
            public <T extends Number> double aggregate(List<T> values) {
                return values.stream()
                        .mapToDouble(Number::doubleValue)
                        .min()
                        .orElseThrow(() -> new NoSuchElementException("Empty list"));
            }
        },
        MAX {
            @Override
            public <T extends Number> double aggregate(List<T> values) {
                return values.stream()
                        .mapToDouble(Number::doubleValue)
                        .max()
                        .orElseThrow(() -> new NoSuchElementException("Empty list"));
            }
        },
        COUNT {
            @Override
            public <T extends Number> double aggregate(List<T> values) {
                return values.size();
            }
        };

        /**
         * Aggregates a list of numbers.
         *
         * @param values the values to aggregate
         * @param <T>    a numeric type
         * @return the aggregated result as a double
         */
        public abstract <T extends Number> double aggregate(List<T> values);
    }

    // --- Example 4: Enum with generic factory methods ---

    /**
     * Enum that acts as a factory for different collection types.
     */
    public enum CollectionFactory {
        ARRAY_LIST {
            @Override
            public <T> Collection<T> create() {
                return new ArrayList<>();
            }

            @Override
            public <T> Collection<T> createFrom(Collection<T> source) {
                return new ArrayList<>(source);
            }
        },
        LINKED_LIST {
            @Override
            public <T> Collection<T> create() {
                return new LinkedList<>();
            }

            @Override
            public <T> Collection<T> createFrom(Collection<T> source) {
                return new LinkedList<>(source);
            }
        },
        HASH_SET {
            @Override
            public <T> Collection<T> create() {
                return new HashSet<>();
            }

            @Override
            public <T> Collection<T> createFrom(Collection<T> source) {
                return new HashSet<>(source);
            }
        },
        TREE_SET {
            @Override
            public <T> Collection<T> create() {
                return new TreeSet<>();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Collection<T> createFrom(Collection<T> source) {
                TreeSet<T> set = new TreeSet<>();
                set.addAll(source);
                return set;
            }
        };

        /**
         * Creates an empty collection.
         */
        public abstract <T> Collection<T> create();

        /**
         * Creates a collection pre-populated from a source.
         */
        public abstract <T> Collection<T> createFrom(Collection<T> source);

        /**
         * Creates a collection with varargs elements.
         */
        @SafeVarargs
        public final <T> Collection<T> of(T... elements) {
            Collection<T> collection = create();
            Collections.addAll(collection, elements);
            return collection;
        }
    }

    // --- Example 5: Enum with generic comparison methods ---

    /**
     * Comparison strategy enum with generic methods.
     */
    public enum CompareStrategy {
        NATURAL {
            @Override
            @SuppressWarnings("unchecked")
            public <T> int compare(T a, T b) {
                if (!(a instanceof Comparable)) {
                    throw new UnsupportedOperationException(
                            a.getClass().getSimpleName() + " is not Comparable");
                }
                return ((Comparable<T>) a).compareTo(b);
            }
        },
        REVERSE {
            @Override
            @SuppressWarnings("unchecked")
            public <T> int compare(T a, T b) {
                if (!(a instanceof Comparable)) {
                    throw new UnsupportedOperationException(
                            a.getClass().getSimpleName() + " is not Comparable");
                }
                return ((Comparable<T>) b).compareTo(a);
            }
        },
        BY_HASH_CODE {
            @Override
            public <T> int compare(T a, T b) {
                return Integer.compare(
                        Objects.hashCode(a),
                        Objects.hashCode(b)
                );
            }
        },
        BY_STRING {
            @Override
            public <T> int compare(T a, T b) {
                return String.valueOf(a).compareTo(String.valueOf(b));
            }
        };

        /**
         * Compares two objects according to this strategy.
         */
        public abstract <T> int compare(T a, T b);

        /**
         * Returns a Comparator backed by this strategy.
         */
        public <T> Comparator<T> toComparator() {
            return this::compare;
        }

        /**
         * Finds the minimum element in a list using this strategy.
         */
        public <T> T min(List<T> items) {
            if (items.isEmpty()) {
                throw new NoSuchElementException("List is empty");
            }
            return items.stream()
                    .min(this::compare)
                    .orElseThrow();
        }

        /**
         * Finds the maximum element in a list using this strategy.
         */
        public <T> T max(List<T> items) {
            if (items.isEmpty()) {
                throw new NoSuchElementException("List is empty");
            }
            return items.stream()
                    .max(this::compare)
                    .orElseThrow();
        }
    }
}
