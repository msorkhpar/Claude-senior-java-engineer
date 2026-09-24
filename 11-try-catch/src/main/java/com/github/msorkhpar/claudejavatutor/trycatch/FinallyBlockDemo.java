package com.github.msorkhpar.claudejavatutor.trycatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class FinallyBlockDemo {

    /**
     * Reads the first line of a UTF-8 text file, closing the file in a finally block
     * (the pre-Java 7 pattern that try-with-resources replaces, see TryWithResourcesDemo).
     *
     * @return the first line, or null if the file is empty
     * @throws java.io.FileNotFoundException if the file does not exist (nothing was opened, so nothing to close)
     */
    public static String readFirstLineFromFile(String path) throws IOException {
        return readFirstLine(new FileReader(path, StandardCharsets.UTF_8));
    }

    /**
     * Reads the first line from the source and always closes it, whether readLine()
     * returns normally or throws.
     */
    static String readFirstLine(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        try {
            return reader.readLine();
        } finally {
            reader.close(); // also closes the wrapped source
        }
    }

    public static int demonstrateFinally() {
        try {
            System.out.println("In try block");
            return 1;
        } finally {
            System.out.println("In finally block");
        }
    }

    public static void demonstrateExceptionInFinally() throws Exception {
        try {
            throw new IllegalArgumentException("Exception from try block");
        } finally {
            throw new RuntimeException("Exception from finally block");
        }
    }
}