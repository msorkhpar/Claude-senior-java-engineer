package com.github.msorkhpar.claudejavatutor.literals;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class StringBuilderBufferTest {

    @Test
    void testUseStringBuilder() {
        String result = StringBuilderBuffer.useStringBuilder("Hello", " ", "World", "!");
        assertThat(result).isEqualTo("Hello World!");
    }

    @Test
    void testUseStringBuffer() {
        String result = StringBuilderBuffer.useStringBuffer("Hello", " ", "World", "!");
        assertThat(result).isEqualTo("Hello World!");
    }

    @Test
    void testReverseWithStringBuilder() {
        String result = StringBuilderBuffer.reverseWithStringBuilder("Hello");
        assertThat(result).isEqualTo("olleH");
    }

    @Test
    void testInsertWithStringBuffer() {
        String result = StringBuilderBuffer.insertWithStringBuffer("Hello World", "Beautiful ", 6);
        assertThat(result).isEqualTo("Hello Beautiful World");
    }

    @Test
    void testDeleteWithStringBuilder() {
        String result = StringBuilderBuffer.deleteWithStringBuilder("Hello World", 5, 11);
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    void testGetStringBuilderCapacity() {
        int capacity = StringBuilderBuffer.getStringBuilderCapacity(50);
        assertThat(capacity).isEqualTo(50);
    }

    @Test
    void testAppendNull() {
        String result = StringBuilderBuffer.useStringBuilder("Test", null, "!");
        assertThat(result).isEqualTo("Testnull!");
    }

    @Test
    void testLargeStringConcatenation() {
        String[] largeArray = new String[10000];
        java.util.Arrays.fill(largeArray, "a");
        String result = StringBuilderBuffer.useStringBuilder(largeArray);
        assertThat(result).hasSize(10000);
        assertThat(result.toCharArray()).containsOnly('a');
    }

    @Test
    void testNegativeCapacity() {
        assertThatThrownBy(() -> new StringBuilder(-1))
                .isInstanceOf(NegativeArraySizeException.class);
    }
}