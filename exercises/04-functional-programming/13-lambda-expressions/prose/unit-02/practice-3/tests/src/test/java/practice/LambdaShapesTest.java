package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaShapesTest {

    @Test
    void zeroOneAndTwoParameters() {
        assertThat(LambdaShapes.greeting().get()).isEqualTo("Hello, World!");
        assertThat(LambdaShapes.length().apply("hello")).isEqualTo(5);
        assertThat(LambdaShapes.length().apply("")).isZero();
        assertThat(LambdaShapes.sum().apply(2, 3)).isEqualTo(5);
    }

    @Test
    void threeParameters() {
        assertThat(LambdaShapes.repeater().apply("ab", 3, true)).isEqualTo("ABABAB");
        assertThat(LambdaShapes.repeater().apply("x", 0, true)).isEmpty();
    }

    @Test
    void varargsSums() {
        assertThat(LambdaShapes.summer().sum(1, 2, 3, 4, 5)).isEqualTo(15);
        assertThat(LambdaShapes.summer().sum(7)).isEqualTo(7);
        assertThat(LambdaShapes.summer().sum(new int[] {-2, 2})).isZero();
    }

    @Test
    void noArgumentsSumToZero() {
        assertThat(LambdaShapes.summer().sum()).isZero();
    }

    @Test
    void repeaterKeepsCaseWhenNotAsked() {
        assertThat(LambdaShapes.repeater().apply("Ab", 2, false)).isEqualTo("AbAb");
    }
}
