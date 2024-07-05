package com.github.msorkhpar.claudejavatutor.trycatch;

import java.io.FileInputStream;
import java.io.IOException;

public class FinallyBlockDemo {

    public static String readFirstLineFromFile(String path) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            return readFirstLine(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static String readFirstLine(FileInputStream inputStream) throws IOException {
        // Implementation omitted for brevity
        return "First line of the file";
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