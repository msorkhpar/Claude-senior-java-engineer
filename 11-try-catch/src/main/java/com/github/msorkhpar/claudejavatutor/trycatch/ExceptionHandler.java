package com.github.msorkhpar.claudejavatutor.trycatch;

import java.io.IOException;
import java.sql.SQLException;

public class ExceptionHandler {

    public static String handleMultipleExceptions(String input) {
        try {
            if (input == null) {
                throw new NullPointerException("Input is null");
            }
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Input is empty");
            }
            if (input.equals("IOException")) {
                throw new IOException("Simulated IOException");
            }
            if (input.equals("SQLException")) {
                throw new SQLException("Simulated SQLException");
            }
            return "Input processed successfully: " + input;
        } catch (NullPointerException | IllegalArgumentException e) {
            return "Invalid input: " + e.getMessage();
        } catch (IOException | SQLException e) {
            return "Database or I/O error: " + e.getMessage();
        } catch (Exception e) {
            return "Unexpected error: " + e.getMessage();
        }
    }

    public static float divideNumber(String input) {
        try {
            int number = Integer.parseInt(input);
            if (number < 0) {
                throw new IllegalArgumentException("Number must be non-negative");
            }
            return 100 / number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format", e);
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Cannot divide by zero");
        }
    }
}