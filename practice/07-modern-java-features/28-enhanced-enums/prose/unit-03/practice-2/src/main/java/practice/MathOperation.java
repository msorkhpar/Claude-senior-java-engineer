package practice;

import java.util.Arrays;
import java.util.Optional;

public enum MathOperation {
    ADD("+") {
        @Override
        public double apply(double a, double b) {
            throw new UnsupportedOperationException("write apply");
        }
    },
    SUBTRACT("-") {
        @Override
        public double apply(double a, double b) {
            throw new UnsupportedOperationException("write apply");
        }
    },
    MULTIPLY("*") {
        @Override
        public double apply(double a, double b) {
            throw new UnsupportedOperationException("write apply");
        }
    },
    DIVIDE("/") {
        @Override
        public double apply(double a, double b) {
            throw new UnsupportedOperationException("write apply");
        }
    },
    MODULUS("%") {
        @Override
        public double apply(double a, double b) {
            throw new UnsupportedOperationException("write apply");
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
        throw new UnsupportedOperationException("write fromSymbol");
    }

    /** The simple name of the constant's enum type, a dot, and the constant's name. */
    public static String qualifiedName(Enum<?> constant) {
        throw new UnsupportedOperationException("write qualifiedName");
    }
}
