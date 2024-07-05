package com.github.msorkhpar.claudejavatutor.trycatch;

import java.io.IOException;
import java.util.logging.Logger;

public class ExceptionHandlingExample {
    private static final Logger logger = Logger.getLogger(ExceptionHandlingExample.class.getName());

    public int divideNumbers(int numerator, int denominator) {
        try {
            return numerator / denominator;
        } catch (ArithmeticException e) {
            logger.warning("Division by zero attempted: " + e.getMessage());
            return 0; // Default value in case of division by zero
        }
    }

    public int getStringLength(String str) {
        try {
            return str.length();
        } catch (NullPointerException e) {
            logger.warning("Null string provided: " + e.getMessage());
            return -1; // Indicating an error
        }
    }

    public void demonstrateExceptionChaining() throws CustomException {
        try {
            throw new IOException("Simulated IO error");
        } catch (IOException e) {
            throw new CustomException("Error in file processing", e);
        }
    }

    // Custom exception for demonstration
    public static class CustomException extends Exception {
        public CustomException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}