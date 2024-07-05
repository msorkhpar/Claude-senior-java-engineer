package com.github.msorkhpar.claudejavatutor.exceptions;

import java.io.IOException;

public class ExceptionDemo {

    // Custom checked exception
    public static class CustomCheckedException extends Exception {
        public CustomCheckedException(String message) {
            super(message);
        }

        public CustomCheckedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Custom unchecked exception
    public static class CustomUncheckedException extends RuntimeException {
        public CustomUncheckedException(String message) {
            super(message);
        }

        public CustomUncheckedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Method that throws a custom checked exception
    public static void methodWithCheckedException(boolean throwException) throws CustomCheckedException {
        if (throwException) {
            throw new CustomCheckedException("This is a custom checked exception");
        }
    }

    // Method that throws a custom unchecked exception
    public static void methodWithUncheckedException(boolean throwException) {
        if (throwException) {
            throw new CustomUncheckedException("This is a custom unchecked exception");
        }
    }

    // Method that demonstrates exception chaining
    public static void methodWithExceptionChaining() throws CustomCheckedException {
        try {
            throw new IOException("Original IO exception");
        } catch (IOException e) {
            throw new CustomCheckedException("Wrapped exception", e);
        }
    }
}