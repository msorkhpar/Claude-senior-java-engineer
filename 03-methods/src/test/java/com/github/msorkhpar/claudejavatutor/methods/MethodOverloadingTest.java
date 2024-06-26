package com.github.msorkhpar.claudejavatutor.methods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

class MethodOverloadingTest {

    private MethodOverloading demo;

    @BeforeEach
    void setUp() {
        demo = new MethodOverloading();
    }

    @Test
    @DisplayName("Test overloaded add methods with different number of parameters")
    void testAddWithDifferentParameterCount() {
        assertThat(demo.add(5, 3)).isEqualTo(8);
        assertThat(demo.add(5, 3, 2)).isEqualTo(10);
    }

    @Test
    @DisplayName("Test overloaded add methods with different types of parameters")
    void testAddWithDifferentParameterTypes() {
        assertThat(demo.add(5.5, 3.2)).isEqualTo(8.7, within(0.001));
        assertThat(demo.add("Hello", "World")).isEqualTo("HelloWorld");
    }

    @Test
    @DisplayName("Test overloaded concat methods with different parameter order")
    void testConcatWithDifferentParameterOrder() {
        assertThat(demo.concat("Age: ", 30)).isEqualTo("Age: 30");
        assertThat(demo.concat(30, " years old")).isEqualTo("30 years old");
    }

    @Test
    @DisplayName("Test overloaded print methods with potential ambiguity")
    void testPrintWithPotentialAmbiguity() {
        demo.print("Hello"); // Should call the String version
        demo.print(new Object()); // Should call the Object version
        // Both calls above are to verify compilation. We're not asserting console output here.
    }

    @Test
    @DisplayName("Test varargs method")
    void testVarargsMethod() {
        assertThat(demo.sum(1, 2, 3)).isEqualTo(6);
        assertThat(demo.sum(1, 2, 3, 4, 5)).isEqualTo(15);
        assertThat(demo.sum()).isEqualTo(0);
    }
}