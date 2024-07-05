package com.github.msorkhpar.claudejavatutor.trycatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TryWithResourcesDemo {

    public static String readFirstLineFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            return br.readLine();
        }
    }

    public static void processMultipleResources(String path1, String path2) throws IOException {
        try (BufferedReader br1 = new BufferedReader(new FileReader(path1));
             BufferedReader br2 = new BufferedReader(new FileReader(path2))) {
            System.out.println(br1.readLine() + " " + br2.readLine());
        }
    }

    public static class CustomResource implements AutoCloseable {
        private final String name;
        private boolean isClosed = false;

        public CustomResource(String name) {
            this.name = name;
        }

        public void doSomething() {
            if (isClosed) {
                throw new IllegalStateException("Resource is already closed");
            }
            System.out.println("Doing something with " + name);
        }

        @Override
        public void close() {
            if (!isClosed) {
                System.out.println("Closing " + name);
                isClosed = true;
            }
        }

        public boolean isClosed() {
            return isClosed;
        }
    }

    public static void useCustomResource() {
        try (CustomResource resource = new CustomResource("MyResource")) {
            resource.doSomething();
        }
    }
}