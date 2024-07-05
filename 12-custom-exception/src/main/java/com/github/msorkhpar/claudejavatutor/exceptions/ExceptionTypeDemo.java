package com.github.msorkhpar.claudejavatutor.exceptions;

import java.io.IOException;

public class ExceptionTypeDemo {

    // Method demonstrating a checked exception
    public void methodWithCheckedException() throws IOException {
        throw new IOException("This is a checked exception");
    }

    // Method demonstrating an unchecked exception
    public void methodWithUncheckedException() {
        throw new IllegalArgumentException("This is an unchecked exception");
    }

    // Method demonstrating exception translation
    public void exceptionTranslationDemo() {
        try {

            methodWithCheckedException();
        } catch (IOException e) {
            throw new RuntimeException("Translated exception", e);
        }
    }

    // Method demonstrating proper resource management with try-with-resources
    public String readFirstLineFromFile(String path) throws IOException {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(path))) {
            return br.readLine();
        }
    }
}