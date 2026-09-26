package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class LengthRuleTest {

    @Test
    void keepsWordsLongEnough() {
        LengthRule five = new LengthRule(5);
        assertThat(five.accepts("hello")).isTrue();
        assertThat(five.accepts("hi")).isFalse();
        assertThat(five.asPredicate().test("hello")).isTrue();
        assertThat(five.asPredicate().test("hi")).isFalse();
        assertThat(new LengthRule(3).keep(List.of("a", "abc", "abcd"))).containsExactly("abc", "abcd");
    }

    @Test
    void eachRuleKeepsItsOwnMinimum() {
        LengthRule five = new LengthRule(5);
        Predicate<String> fivePredicate = five.asPredicate();
        LengthRule two = new LengthRule(2);
        assertThat(five.accepts("abc")).isFalse();
        assertThat(fivePredicate.test("abc")).isFalse();
        assertThat(two.accepts("abc")).isTrue();
        assertThat(five.keep(List.of("abc", "abcdef"))).containsExactly("abcdef");
    }

    @Test
    void nullIsNeverAccepted() {
        LengthRule zero = new LengthRule(0);
        assertThat(zero.accepts(null)).isFalse();
        assertThat(zero.keep(Arrays.asList("a", null, "b"))).containsExactly("a", "b");
    }
}
