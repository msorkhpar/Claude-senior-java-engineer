package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionalTest {

    @FunctionalInterface
    public interface Transformer<T, R> {
        R transform(T input);

        default <V> Transformer<T, V> andThen(Transformer<R, V> after) {
            return input -> after.transform(this.transform(input));
        }
    }

    public interface TwoJobs {
        void first();

        void second();
    }

    @FunctionalInterface
    public interface Validator<T> {
        boolean validate(T input);

        @Override
        String toString();

        @Override
        boolean equals(Object other);
    }

    public interface Matcher {
        boolean matches(String input);

        boolean equals(String other);
    }

    public interface Copier {
        void copy();

        Object clone();
    }

    public interface Parser {
        int parse(String text);

        static Parser of() {
            return Integer::parseInt;
        }
    }

    public interface Named extends Transformer<String, String> {
    }

    public interface Wider extends Transformer<String, String> {
        void reset();
    }

    public abstract static class Task {
        public abstract void run();
    }

    @Test
    void oneAbstractMethodIsFunctional() {
        assertThat(Functional.isFunctional(Transformer.class)).isTrue();
        assertThat(Functional.isFunctional(TwoJobs.class)).isFalse();
    }

    @Test
    void objectMethodsDoNotCount() {
        assertThat(Functional.isFunctional(Validator.class)).isTrue();
    }

    @Test
    void anEqualsOverloadCounts() {
        assertThat(Functional.isFunctional(Matcher.class)).isFalse();
        assertThat(Functional.isFunctional(Copier.class)).isFalse();
    }

    @Test
    void staticMethodsDoNotCount() {
        assertThat(Functional.isFunctional(Parser.class)).isTrue();
    }

    @Test
    void inheritedAbstractMethodsCount() {
        assertThat(Functional.isFunctional(Named.class)).isTrue();
        assertThat(Functional.isFunctional(Wider.class)).isFalse();
    }

    @Test
    void onlyAnInterfaceQualifies() {
        assertThat(Functional.isFunctional(Task.class)).isFalse();
    }
}
