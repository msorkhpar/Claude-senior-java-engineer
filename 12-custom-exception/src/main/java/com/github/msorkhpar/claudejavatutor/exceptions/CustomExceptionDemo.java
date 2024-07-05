package com.github.msorkhpar.claudejavatutor.exceptions;

import java.io.IOException;

public class CustomExceptionDemo {

    public static class InvalidUserDataException extends RuntimeException {
        public InvalidUserDataException(String message) {
            super(message);
        }

        public InvalidUserDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class FileProcessingException extends Exception {
        private final String fileName;

        public FileProcessingException(String message, String fileName) {
            super(message);
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserDataException("Username cannot be null or empty");
        }
        // Further validation logic...
    }

    public void processFile(String fileName) throws FileProcessingException {
        try {
            // Simulating file processing that might throw an IOException
            if (fileName.equals("nonexistent.txt")) {
                throw new IOException("File not found");
            }
            // File processing logic...
        } catch (IOException e) {
            throw new FileProcessingException("Error processing file: " + fileName, fileName);
        }
    }
}