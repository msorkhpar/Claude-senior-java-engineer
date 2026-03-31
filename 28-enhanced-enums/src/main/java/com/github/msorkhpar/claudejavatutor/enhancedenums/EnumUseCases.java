package com.github.msorkhpar.claudejavatutor.enhancedenums;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Demonstrates real-world use cases and advanced patterns for enhanced enums in Java.
 * Covers state machines, strategy patterns, singleton services, validation frameworks,
 * and builder-like enum configurations.
 */
public class EnumUseCases {

    // --- Use Case 1: State Machine ---

    /**
     * Order lifecycle state machine implemented as an enum.
     * Each state knows which transitions are valid.
     */
    public enum OrderState {
        CREATED {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(PENDING_PAYMENT, CANCELLED);
            }
        },
        PENDING_PAYMENT {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(PAID, CANCELLED);
            }
        },
        PAID {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(PROCESSING, REFUNDED);
            }
        },
        PROCESSING {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(SHIPPED, CANCELLED);
            }
        },
        SHIPPED {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(DELIVERED, RETURNED);
            }
        },
        DELIVERED {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(RETURNED);
            }
        },
        CANCELLED {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.noneOf(OrderState.class);
            }
        },
        REFUNDED {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.noneOf(OrderState.class);
            }
        },
        RETURNED {
            @Override
            public Set<OrderState> validTransitions() {
                return EnumSet.of(REFUNDED);
            }
        };

        public abstract Set<OrderState> validTransitions();

        public boolean canTransitionTo(OrderState target) {
            return validTransitions().contains(target);
        }

        public OrderState transitionTo(OrderState target) {
            if (!canTransitionTo(target)) {
                throw new IllegalStateException(
                        "Cannot transition from " + this + " to " + target);
            }
            return target;
        }

        public boolean isTerminal() {
            return validTransitions().isEmpty();
        }
    }

    // --- Use Case 2: Strategy Pattern ---

    /**
     * Discount strategy enum implementing the Strategy pattern.
     */
    public enum DiscountStrategy {
        NONE("No Discount", amount -> amount),
        PERCENTAGE_10("10% Off", amount -> amount * 0.90),
        PERCENTAGE_20("20% Off", amount -> amount * 0.80),
        FLAT_5("$5 Off", amount -> Math.max(0, amount - 5.0)),
        FLAT_10("$10 Off", amount -> Math.max(0, amount - 10.0)),
        BUY_ONE_GET_HALF("Buy 1 Get 50% Off 2nd", amount -> amount * 0.75);

        private final String description;
        private final Function<Double, Double> calculator;

        DiscountStrategy(String description, Function<Double, Double> calculator) {
            this.description = description;
            this.calculator = calculator;
        }

        public String description() {
            return description;
        }

        public double applyDiscount(double originalAmount) {
            if (originalAmount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            return Math.round(calculator.apply(originalAmount) * 100.0) / 100.0;
        }

        /**
         * Returns the best discount for a given amount.
         */
        public static DiscountStrategy bestDiscount(double amount) {
            return Arrays.stream(values())
                    .filter(d -> d != NONE)
                    .min(Comparator.comparingDouble(d -> d.applyDiscount(amount)))
                    .orElse(NONE);
        }
    }

    // --- Use Case 3: Validation Rules ---

    /**
     * Validation rule enum for validating user input.
     */
    public enum ValidationRule {
        NOT_NULL("Value must not be null", Objects::nonNull),
        NOT_EMPTY("String must not be empty",
                v -> v != null && v instanceof String s && !s.isEmpty()),
        NOT_BLANK("String must not be blank",
                v -> v != null && v instanceof String s && !s.isBlank()),
        POSITIVE_NUMBER("Number must be positive",
                v -> v instanceof Number n && n.doubleValue() > 0),
        NON_NEGATIVE_NUMBER("Number must be non-negative",
                v -> v instanceof Number n && n.doubleValue() >= 0),
        VALID_EMAIL("Must be a valid email format",
                v -> v instanceof String s && s.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));

        private final String message;
        private final Predicate<Object> validator;

        ValidationRule(String message, Predicate<Object> validator) {
            this.message = message;
            this.validator = validator;
        }

        public String message() {
            return message;
        }

        public boolean isValid(Object value) {
            return validator.test(value);
        }

        public Optional<String> validate(Object value) {
            return isValid(value) ? Optional.empty() : Optional.of(message);
        }

        /**
         * Validates a value against multiple rules, returning all violations.
         */
        public static List<String> validateAll(Object value, ValidationRule... rules) {
            return Arrays.stream(rules)
                    .map(rule -> rule.validate(value))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    // --- Use Case 4: Singleton Service Registry ---

    /**
     * Enum-based singleton services (each constant is a unique singleton instance).
     */
    public enum CacheType {
        LRU {
            private final Map<String, Object> cache = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                    return size() > maxSize();
                }
            };

            @Override
            public void put(String key, Object value) {
                cache.put(key, value);
            }

            @Override
            public Optional<Object> get(String key) {
                return Optional.ofNullable(cache.get(key));
            }

            @Override
            public void clear() {
                cache.clear();
            }

            @Override
            public int size() {
                return cache.size();
            }
        },
        FIFO {
            private final LinkedHashMap<String, Object> cache = new LinkedHashMap<>();

            @Override
            public void put(String key, Object value) {
                if (cache.size() >= maxSize() && !cache.containsKey(key)) {
                    String firstKey = cache.keySet().iterator().next();
                    cache.remove(firstKey);
                }
                cache.put(key, value);
            }

            @Override
            public Optional<Object> get(String key) {
                return Optional.ofNullable(cache.get(key));
            }

            @Override
            public void clear() {
                cache.clear();
            }

            @Override
            public int size() {
                return cache.size();
            }
        };

        protected int maxSize() {
            return 100;
        }

        public abstract void put(String key, Object value);

        public abstract Optional<Object> get(String key);

        public abstract void clear();

        public abstract int size();
    }

    // --- Use Case 5: Enum for pattern matching with sealed types ---

    /**
     * Result type using enum for status and sealed interface for detail.
     */
    public enum ResultStatus {
        SUCCESS, FAILURE, PENDING;

        public boolean isSuccessful() {
            return this == SUCCESS;
        }

        public boolean isFailed() {
            return this == FAILURE;
        }

        public boolean isPending() {
            return this == PENDING;
        }
    }

    /**
     * A result container that combines enum status with a generic value.
     */
    public record Result<T>(ResultStatus status, T value, String message) {

        public static <T> Result<T> success(T value) {
            return new Result<>(ResultStatus.SUCCESS, value, "OK");
        }

        public static <T> Result<T> failure(String message) {
            return new Result<>(ResultStatus.FAILURE, null, message);
        }

        public static <T> Result<T> pending() {
            return new Result<>(ResultStatus.PENDING, null, "Pending");
        }

        public boolean isSuccessful() {
            return status.isSuccessful();
        }

        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }

        /**
         * Maps the value if successful, otherwise returns the same failure/pending.
         */
        public <R> Result<R> map(Function<T, R> mapper) {
            if (isSuccessful() && value != null) {
                return Result.success(mapper.apply(value));
            }
            return new Result<>(status, null, message);
        }

        /**
         * FlatMaps the value if successful.
         */
        public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
            if (isSuccessful() && value != null) {
                return mapper.apply(value);
            }
            return new Result<>(status, null, message);
        }
    }

    // --- Use Case 6: EnumSet and EnumMap usage patterns ---

    /**
     * Permission system using EnumSet for efficient flag combinations.
     */
    public enum Permission {
        READ, WRITE, EXECUTE, DELETE, ADMIN;

        public static final Set<Permission> READ_ONLY = EnumSet.of(READ);
        public static final Set<Permission> READ_WRITE = EnumSet.of(READ, WRITE);
        public static final Set<Permission> FULL_ACCESS = EnumSet.allOf(Permission.class);
        public static final Set<Permission> NO_ACCESS = EnumSet.noneOf(Permission.class);

        /**
         * Checks if a set of permissions includes this permission.
         */
        public boolean isGrantedIn(Set<Permission> permissions) {
            return permissions.contains(this);
        }

        /**
         * Combines multiple permission sets using union.
         */
        @SafeVarargs
        public static Set<Permission> combine(Set<Permission>... sets) {
            EnumSet<Permission> combined = EnumSet.noneOf(Permission.class);
            for (Set<Permission> set : sets) {
                combined.addAll(set);
            }
            return Collections.unmodifiableSet(combined);
        }

        /**
         * Finds the intersection of multiple permission sets.
         */
        @SafeVarargs
        public static Set<Permission> intersect(Set<Permission>... sets) {
            if (sets.length == 0) {
                return EnumSet.noneOf(Permission.class);
            }
            EnumSet<Permission> result = EnumSet.copyOf(sets[0]);
            for (int i = 1; i < sets.length; i++) {
                result.retainAll(sets[i]);
            }
            return Collections.unmodifiableSet(result);
        }
    }
}
