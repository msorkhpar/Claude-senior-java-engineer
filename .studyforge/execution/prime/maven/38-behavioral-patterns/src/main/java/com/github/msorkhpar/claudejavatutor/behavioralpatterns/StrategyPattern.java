package com.github.msorkhpar.claudejavatutor.behavioralpatterns;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Demonstrates the Strategy design pattern in Java.
 * The Strategy pattern defines a family of algorithms, encapsulates each one,
 * and makes them interchangeable at runtime.
 */
public class StrategyPattern {

    // ===================== Classic Strategy (Interface-based) =====================

    /**
     * Strategy interface for sorting algorithms.
     */
    public interface SortStrategy<T extends Comparable<T>> {
        List<T> sort(List<T> data);

        String name();
    }

    /**
     * Bubble sort strategy implementation.
     */
    public static class BubbleSortStrategy<T extends Comparable<T>> implements SortStrategy<T> {
        @Override
        public List<T> sort(List<T> data) {
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null");
            }
            List<T> result = new ArrayList<>(data);
            int n = result.size();
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (result.get(j).compareTo(result.get(j + 1)) > 0) {
                        T temp = result.get(j);
                        result.set(j, result.get(j + 1));
                        result.set(j + 1, temp);
                    }
                }
            }
            return result;
        }

        @Override
        public String name() {
            return "BubbleSort";
        }
    }

    /**
     * Selection sort strategy implementation.
     */
    public static class SelectionSortStrategy<T extends Comparable<T>> implements SortStrategy<T> {
        @Override
        public List<T> sort(List<T> data) {
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null");
            }
            List<T> result = new ArrayList<>(data);
            int n = result.size();
            for (int i = 0; i < n - 1; i++) {
                int minIdx = i;
                for (int j = i + 1; j < n; j++) {
                    if (result.get(j).compareTo(result.get(minIdx)) < 0) {
                        minIdx = j;
                    }
                }
                T temp = result.get(minIdx);
                result.set(minIdx, result.get(i));
                result.set(i, temp);
            }
            return result;
        }

        @Override
        public String name() {
            return "SelectionSort";
        }
    }

    /**
     * Java built-in sort strategy (delegates to Collections.sort).
     */
    public static class JavaSortStrategy<T extends Comparable<T>> implements SortStrategy<T> {
        @Override
        public List<T> sort(List<T> data) {
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null");
            }
            List<T> result = new ArrayList<>(data);
            Collections.sort(result);
            return result;
        }

        @Override
        public String name() {
            return "JavaSort";
        }
    }

    /**
     * Context class that uses a sorting strategy.
     */
    public static class Sorter<T extends Comparable<T>> {
        private SortStrategy<T> strategy;

        public Sorter(SortStrategy<T> strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy cannot be null");
            }
            this.strategy = strategy;
        }

        public void setStrategy(SortStrategy<T> strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy cannot be null");
            }
            this.strategy = strategy;
        }

        public SortStrategy<T> getStrategy() {
            return strategy;
        }

        public List<T> sort(List<T> data) {
            return strategy.sort(data);
        }
    }

    // ===================== Lambda-based Strategy (Modern Java) =====================

    /**
     * Demonstrates using lambda expressions and functional interfaces as strategies.
     */
    public static class TextProcessor {
        private UnaryOperator<String> strategy;

        public TextProcessor(UnaryOperator<String> strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy cannot be null");
            }
            this.strategy = strategy;
        }

        public void setStrategy(UnaryOperator<String> strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy cannot be null");
            }
            this.strategy = strategy;
        }

        public String process(String text) {
            if (text == null) {
                throw new IllegalArgumentException("Text cannot be null");
            }
            return strategy.apply(text);
        }

        /**
         * Predefined strategies as static constants.
         */
        public static final UnaryOperator<String> UPPER_CASE = String::toUpperCase;
        public static final UnaryOperator<String> LOWER_CASE = String::toLowerCase;
        public static final UnaryOperator<String> REVERSE = s -> new StringBuilder(s).reverse().toString();
        public static final UnaryOperator<String> TRIM_AND_UPPER = s -> s.trim().toUpperCase();
        public static final UnaryOperator<String> REMOVE_WHITESPACE = s -> s.replaceAll("\\s+", "");
    }

    // ===================== Strategy with Sealed Interfaces (Java 17+) =====================

    /**
     * Payment strategy using sealed interfaces for exhaustive pattern matching.
     */
    public sealed interface PaymentStrategy permits CreditCardPayment, PayPalPayment, CryptoPayment {
        PaymentResult pay(double amount);

        String methodName();
    }

    public record PaymentResult(boolean success, String transactionId, String message) {
    }

    public record CreditCardPayment(String cardNumber, String expiryDate) implements PaymentStrategy {
        public CreditCardPayment {
            if (cardNumber == null || cardNumber.isBlank()) {
                throw new IllegalArgumentException("Card number cannot be null or blank");
            }
            if (expiryDate == null || expiryDate.isBlank()) {
                throw new IllegalArgumentException("Expiry date cannot be null or blank");
            }
        }

        @Override
        public PaymentResult pay(double amount) {
            if (amount <= 0) {
                return new PaymentResult(false, null, "Amount must be positive");
            }
            String txnId = "CC-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txnId, "Paid %.2f via credit card ending in %s".formatted(
                    amount, cardNumber.substring(cardNumber.length() - 4)));
        }

        @Override
        public String methodName() {
            return "CreditCard";
        }
    }

    public record PayPalPayment(String email) implements PaymentStrategy {
        public PayPalPayment {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email cannot be null or blank");
            }
        }

        @Override
        public PaymentResult pay(double amount) {
            if (amount <= 0) {
                return new PaymentResult(false, null, "Amount must be positive");
            }
            String txnId = "PP-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txnId, "Paid %.2f via PayPal (%s)".formatted(amount, email));
        }

        @Override
        public String methodName() {
            return "PayPal";
        }
    }

    public record CryptoPayment(String walletAddress) implements PaymentStrategy {
        public CryptoPayment {
            if (walletAddress == null || walletAddress.isBlank()) {
                throw new IllegalArgumentException("Wallet address cannot be null or blank");
            }
        }

        @Override
        public PaymentResult pay(double amount) {
            if (amount <= 0) {
                return new PaymentResult(false, null, "Amount must be positive");
            }
            String txnId = "CRYPTO-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txnId, "Paid %.2f via crypto wallet".formatted(amount));
        }

        @Override
        public String methodName() {
            return "Crypto";
        }
    }

    /**
     * Payment processor (context) that uses pattern matching with sealed strategy.
     */
    public static class PaymentProcessor {
        private PaymentStrategy strategy;

        public PaymentProcessor(PaymentStrategy strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy cannot be null");
            }
            this.strategy = strategy;
        }

        public void setStrategy(PaymentStrategy strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy cannot be null");
            }
            this.strategy = strategy;
        }

        public PaymentResult processPayment(double amount) {
            return strategy.pay(amount);
        }

        /**
         * Demonstrates exhaustive pattern matching on sealed strategy.
         */
        public String describeStrategy() {
            return switch (strategy) {
                case CreditCardPayment cc -> "Credit card ending in " + cc.cardNumber().substring(cc.cardNumber().length() - 4);
                case PayPalPayment pp -> "PayPal account: " + pp.email();
                case CryptoPayment cp -> "Crypto wallet: " + cp.walletAddress();
            };
        }
    }

    // ===================== Strategy with Composition =====================

    /**
     * Demonstrates composable strategies using Function chaining.
     */
    public static class DataPipeline<T> {
        private final List<Function<List<T>, List<T>>> stages = new ArrayList<>();

        public DataPipeline<T> addStage(Function<List<T>, List<T>> stage) {
            if (stage == null) {
                throw new IllegalArgumentException("Stage cannot be null");
            }
            stages.add(stage);
            return this;
        }

        public List<T> execute(List<T> input) {
            if (input == null) {
                throw new IllegalArgumentException("Input cannot be null");
            }
            List<T> result = new ArrayList<>(input);
            for (Function<List<T>, List<T>> stage : stages) {
                result = stage.apply(result);
            }
            return result;
        }

        public int stageCount() {
            return stages.size();
        }
    }
}
