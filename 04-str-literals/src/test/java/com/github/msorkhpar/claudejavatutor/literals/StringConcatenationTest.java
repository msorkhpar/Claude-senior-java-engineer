package com.github.msorkhpar.claudejavatutor.literals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class StringConcatenationTest {

    private final Faker faker = new Faker();

    @Test
    void testSimpleConcat() {
        String result = StringConcatenation.simpleConcat("Hello", "World");
        assertThat(result).isEqualTo("HelloWorld");
    }

    @Test
    void testConcatWithNull() {
        String result = StringConcatenation.concatWithNull("Test");
        assertThat(result).isEqualTo("Testnull");
    }

    @Test
    void testConcatInLoop() {
        List<String> strings = IntStream.range(0, 1000)
                .mapToObj(i -> faker.lorem().word())
                .collect(Collectors.toList());

        String result = StringConcatenation.concatInLoop(strings);

        assertThat(result).hasSize(strings.stream().mapToInt(String::length).sum());
        for (String s : strings) {
            assertThat(result).contains(s);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "42, true, 3.14",
            "0, false, 0.0",
            "-1, true, -1.5"
    })
    void testConcatWithDifferentTypes(int number, boolean flag, double value) {
        String result = StringConcatenation.concatWithDifferentTypes(number, flag, value);
        assertThat(result).isEqualTo("Number: " + number + ", Flag: " + flag + ", Value: " + value);
    }

    @Test
    void testConcatLargeStrings() {
        String s1 = "A".repeat(1000);
        String s2 = "B".repeat(500);
        int repeatCount = 100;

        String result = StringConcatenation.concatLargeStrings(s1, s2, repeatCount);

        assertThat(result).hasSize(s1.length() * repeatCount + s2.length());
        assertThat(result).startsWith(s1.repeat(repeatCount));
        assertThat(result).endsWith(s2);
    }

    @Test
    void testConcatPerformance() {
        String s1 = faker.lorem().paragraph();
        String s2 = faker.lorem().paragraph();
        int repeatCount = 10000;

        long start = System.nanoTime();
        StringConcatenation.concatLargeStrings(s1, s2, repeatCount);
        long end = System.nanoTime();

        long duration = (end - start) / 1_000_000; // Convert to milliseconds
        System.out.println("Concatenation took " + duration + " ms");

        Assertions.assertThat(duration)
                .as("Concatenation should complete in a reasonable time")
                .isLessThan(1000); // Assuming it should take less than 1 second
    }
}