package practice;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ChainTest {

    private static Chain.Node chainOf(int... values) {
        Chain.Node head = null;
        for (int i = values.length - 1; i >= 0; i--) {
            head = new Chain.Node(values[i], head);
        }
        return head;
    }

    @Test
    void sumsAndCountsAShortChain() {
        Chain.Node chain = chainOf(3, 4, 5);

        assertThat(Chain.sum(chain)).isEqualTo(12L);
        assertThat(Chain.length(chain)).isEqualTo(3);
        assertThat(Chain.factorial(5)).isEqualTo(120L);
        assertThat(Chain.factorial(0)).isEqualTo(1L);
        assertThat(Chain.factorial(1)).isEqualTo(1L);
    }

    @Test
    void aMillionNodeChainIsWalkedWithALoop() {
        Chain.Node head = null;
        for (int i = 0; i < 1_000_000; i++) {
            head = new Chain.Node(1, head);
        }

        assertThat(Chain.sum(head)).isEqualTo(1_000_000L);
        assertThat(Chain.length(head)).isEqualTo(1_000_000);
    }

    @Test
    void nullIsAnEmptyChain() {
        assertThat(Chain.sum(null)).isZero();
        assertThat(Chain.length(null)).isZero();
    }

    @Test
    void aNegativeFactorialIsRefused() {
        assertThatThrownBy(() -> Chain.factorial(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void factorialThatOverflowsIsRefused() {
        assertThat(Chain.factorial(20)).isEqualTo(2_432_902_008_176_640_000L);
        assertThatThrownBy(() -> Chain.factorial(21)).isInstanceOf(ArithmeticException.class);
    }
}
