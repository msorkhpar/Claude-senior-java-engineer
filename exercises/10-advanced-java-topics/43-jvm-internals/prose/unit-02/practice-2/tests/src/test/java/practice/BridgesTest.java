package practice;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BridgesTest {

    interface Transformer<T> {
        T transform(T input);
    }

    static class Upper implements Transformer<String> {
        @Override
        public String transform(String input) {
            return input.toUpperCase(java.util.Locale.ROOT);
        }
    }

    static class Word implements Comparable<Word> {
        final String text;

        Word(String text) {
            this.text = text;
        }

        @Override
        public int compareTo(Word other) {
            return text.compareTo(other.text);
        }
    }

    static class Shape {
        Shape copy() {
            return new Shape();
        }
    }

    static class Circle extends Shape {
        @Override
        Circle copy() {
            return new Circle();
        }
    }

    static class Greeter {
        Supplier<String> greeting() {
            return () -> "hello from " + getClass().getSimpleName();
        }
    }

    @Test
    void findsTheBridgesErasureAndCovarianceNeed() {
        assertThat(Bridges.bridgesOf(Upper.class)).containsExactly("Object transform(Object)");
        assertThat(Bridges.sourceMethodsOf(Upper.class)).containsExactly("String transform(String)");

        assertThat(Bridges.bridgesOf(Word.class)).containsExactly("int compareTo(Object)");
        assertThat(Bridges.sourceMethodsOf(Word.class)).containsExactly("int compareTo(Word)");

        assertThat(Bridges.bridgesOf(Circle.class)).containsExactly("Shape copy()");
        assertThat(Bridges.sourceMethodsOf(Circle.class)).containsExactly("Circle copy()");

        assertThat(Bridges.bridgesOf(Shape.class)).isEmpty();
    }

    @Test
    void aLambdaBodyIsSyntheticButNotABridge() {
        assertThat(new Greeter().greeting().get()).isEqualTo("hello from Greeter");

        assertThat(Bridges.bridgesOf(Greeter.class)).isEmpty();
    }

    @Test
    void sourceMethodsLeaveOutEverySyntheticMethod() {
        assertThat(Bridges.sourceMethodsOf(Greeter.class)).containsExactly("Supplier greeting()");
    }
}
