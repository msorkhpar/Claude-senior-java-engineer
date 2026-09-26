package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RulesTest {

    private static final Predicate<Integer> POSITIVE = n -> n > 0;
    private static final Predicate<Integer> EVEN = n -> n % 2 == 0;

    @Test
    void allOfNeedsEveryRuleAndAnyOfNeedsOne() {
        Predicate<Integer> both = Rules.allOf(List.of(POSITIVE, EVEN));
        assertThat(both.test(4)).isTrue();
        assertThat(both.test(3)).isFalse();
        assertThat(both.test(-2)).isFalse();

        Predicate<Integer> either = Rules.anyOf(List.of(POSITIVE, EVEN));
        assertThat(either.test(4)).isTrue();
        assertThat(either.test(3)).isTrue();
        assertThat(either.test(-2)).isTrue();
        assertThat(either.test(-3)).isFalse();
    }

    @Test
    void noRulesMeansAllPassAndNoneMatch() {
        assertThat(Rules.<String>allOf(List.of()).test("anything")).isTrue();
        assertThat(Rules.<String>anyOf(List.of()).test("anything")).isFalse();
    }

    @Test
    void laterRulesAreSkippedOnceTheAnswerIsKnown() {
        List<String> asked = new ArrayList<>();
        Predicate<String> expensive = s -> {
            asked.add(s);
            return true;
        };
        Predicate<String> notNull = s -> s != null;
        Predicate<String> isNull = s -> s == null;

        assertThat(Rules.allOf(List.of(notNull, expensive)).test(null)).isFalse();
        assertThat(Rules.anyOf(List.of(isNull, expensive)).test(null)).isTrue();
        assertThat(asked).isEmpty();
    }

    @Test
    void aNullRuleIsRejectedWhenBuilding() {
        List<Predicate<Integer>> withNull = Arrays.asList(POSITIVE, null);

        assertThatThrownBy(() -> Rules.allOf(withNull)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Rules.anyOf(withNull)).isInstanceOf(NullPointerException.class);
    }
}
