package practice;

import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PipelinesTest {

    @Test
    void runsTheBaseThenEachLayer() {
        UnaryOperator<String> logged = Pipelines.decorate(String::trim,
                List.of(String::toUpperCase, s -> "[LOG] " + s));
        UnaryOperator<String> bare = Pipelines.decorate(String::trim, List.of());

        assertThat(logged.apply("  hello  ")).isEqualTo("[LOG] HELLO");
        assertThat(bare.apply("  hello  ")).isEqualTo("hello");
    }

    @Test
    void layersRunInListOrder() {
        UnaryOperator<String> bang = s -> s + "!";
        UnaryOperator<String> twice = s -> s + s;

        assertThat(Pipelines.decorate(UnaryOperator.identity(), List.of(bang, twice)).apply("hello"))
                .isEqualTo("hello!hello!");
        assertThat(Pipelines.decorate(UnaryOperator.identity(), List.of(twice, bang)).apply("hello"))
                .isEqualTo("hellohello!");
    }

    @Test
    void aLayerListedTwiceRunsTwice() {
        UnaryOperator<String> bang = s -> s + "!";

        assertThat(Pipelines.decorate(UnaryOperator.identity(), List.of(bang, bang)).apply("hello"))
                .isEqualTo("hello!!");
    }
}
