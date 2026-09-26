package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineTest {

    @Test
    void inOrderRunsFirstToLastAndInReverseLastToFirst() {
        UnaryOperator<Integer> times2 = x -> x * 2;
        UnaryOperator<Integer> plus3 = x -> x + 3;
        assertThat(Pipeline.inOrder(List.of(times2, plus3)).apply(5)).isEqualTo(13);
        assertThat(Pipeline.inReverse(List.of(times2, plus3)).apply(5)).isEqualTo(16);

        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> bang = s -> s + "!";
        UnaryOperator<String> upper = String::toUpperCase;
        assertThat(Pipeline.inOrder(List.of(trim, bang, upper)).apply(" hi ")).isEqualTo("HI!");
        assertThat(Pipeline.inReverse(List.of(trim, bang, upper)).apply(" hi ")).isEqualTo("HI !");
    }

    @Test
    void noStepsIsTheIdentity() {
        List<String> value = new ArrayList<>(List.of("a"));

        assertThat(Pipeline.<List<String>>inOrder(List.of()).apply(value)).isSameAs(value);
        assertThat(Pipeline.<List<String>>inReverse(List.of()).apply(value)).isSameAs(value);
    }

    @Test
    void aNullResultEndsThePipeline() {
        UnaryOperator<String> toNull = s -> null;
        UnaryOperator<String> upper = String::toUpperCase;

        assertThat(Pipeline.inOrder(List.of(toNull, upper)).apply("hello")).isNull();
        assertThat(Pipeline.inReverse(List.of(upper, toNull)).apply("hello")).isNull();
    }

    @Test
    void aStepsOwnExceptionReachesTheCaller() {
        NullPointerException bug = new NullPointerException("bug in step");
        UnaryOperator<String> broken = s -> {
            throw bug;
        };

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> Pipeline.inOrder(List.of(broken)).apply("x"))
                .isSameAs(bug);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> Pipeline.inReverse(List.of(broken)).apply("x"))
                .isSameAs(bug);
    }

    @Test
    void laterListChangesDoNotChangeTheFunction() {
        List<UnaryOperator<Integer>> steps = new ArrayList<>(List.of(x -> x + 1));
        java.util.function.Function<Integer, Integer> forward = Pipeline.inOrder(steps);
        java.util.function.Function<Integer, Integer> backward = Pipeline.inReverse(steps);

        steps.add(x -> x * 10);

        assertThat(forward.apply(1)).isEqualTo(2);
        assertThat(backward.apply(1)).isEqualTo(2);
    }
}
