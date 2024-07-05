package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TryWithResourcesDemoTest {

    @Test
    void testReadFirstLineFromFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.write(file, "Hello, World!".getBytes());

        String result = TryWithResourcesDemo.readFirstLineFromFile(file.toString());
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void testProcessMultipleResources(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.write(file1, "Hello".getBytes());
        Files.write(file2, "World".getBytes());

        TryWithResourcesDemo.processMultipleResources(file1.toString(), file2.toString());
        // This test mainly checks if the method runs without exceptions
    }

    @Test
    void testCustomResource() {
        TryWithResourcesDemo.useCustomResource();
        // This test mainly checks if the method runs without exceptions
    }

    @Test
    void testCustomResourceClosing() {
        TryWithResourcesDemo.CustomResource resource = new TryWithResourcesDemo.CustomResource("TestResource");
        try (resource) {
            resource.doSomething();
        }
        assertThat(resource.isClosed()).isTrue();
    }

    @Test
    void testCustomResourceThrowsExceptionWhenUsedAfterClosing() {
        TryWithResourcesDemo.CustomResource resource = new TryWithResourcesDemo.CustomResource("TestResource");
        try (resource) {
            resource.doSomething();
        }
        assertThatThrownBy(resource::doSomething)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Resource is already closed");
    }
}