package practice;

import java.util.Arrays;
import java.util.Optional;

public enum MathOperation {
    ADD("+") {
        @Override
        public double apply(double a, double b) {
            return a + b;
        }
    },
    SUBTRACT("-") {
        @Override
        public double apply(double a, double b) {
            return a - b;
        }
    },
    MULTIPLY("*") {
        @Override
        public double apply(double a, double b) {
            return a * b;
        }
    },
    DIVIDE("/") {
        @Override
        public double apply(double a, double b) {
            if (b == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return a / b;
        }
    },
    MODULUS("%") {
        @Override
        public double apply(double a, double b) {
            if (b == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return a % b;
        }
    };

    /** A plain enum: its constants have no bodies. */
    public enum Precedence { LOW, HIGH }

    private final String symbol;

    MathOperation(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public abstract double apply(double a, double b);

    /** The constant whose symbol equals {@code symbol}, or empty. */
    public static Optional<MathOperation> fromSymbol(String symbol) {
        return Arrays.stream(values()).filter(op -> op.symbol == symbol).findFirst();
    }

    /** The simple name of the constant's enum type, a dot, and the constant's name. */
    public static String qualifiedName(Enum<?> constant) {
        return constant.getDeclaringClass().getSimpleName() + "." + constant.name();
    }
}
