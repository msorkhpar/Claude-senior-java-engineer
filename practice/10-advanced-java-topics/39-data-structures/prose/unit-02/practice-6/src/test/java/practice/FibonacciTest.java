package practice;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FibonacciTest {

    /** A memo that counts how often a value is stored in it. */
    private static final class CountingMemo extends HashMap<Integer, Long> {
        int puts;

        @Override
        public Long put(Integer key, Long value) {
            puts++;
            return super.put(key, value);
        }

        @Override
        public Long putIfAbsent(Integer key, Long value) {
            puts++;
            return super.putIfAbsent(key, value);
        }
    }

    @Test
    void computesThePageValues() {
        assertThat(Fibonacci.fib(0, new CountingMemo())).isZero();
        assertThat(Fibonacci.fib(1, new CountingMemo())).isEqualTo(1L);
        assertThat(Fibonacci.fib(10, new CountingMemo())).isEqualTo(55L);
        assertThat(Fibonacci.fib(20, new CountingMemo())).isEqualTo(6_765L);
        assertThatThrownBy(() -> Fibonacci.fib(-1, new CountingMemo())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eachValueIsComputedOnce() {
        CountingMemo memo = new CountingMemo();

        assertThat(Fibonacci.fib(25, memo)).isEqualTo(75_025L);
        assertThat(memo.puts).isBetween(1, 26);
        assertThat(Fibonacci.fib(50, new CountingMemo())).isEqualTo(12_586_269_025L);
        assertThat(Fibonacci.fib(90, new CountingMemo())).isEqualTo(2_880_067_194_370_816_120L);
    }

    @Test
    void aLaterCallReusesTheMemo() {
        CountingMemo memo = new CountingMemo();
        assertThat(Fibonacci.fib(40, memo)).isEqualTo(102_334_155L);
        int before = memo.puts;

        assertThat(Fibonacci.fib(41, memo)).isEqualTo(165_580_141L);
        assertThat(memo.puts - before).isEqualTo(1);
    }

    @Test
    void aResultTooBigForALongIsRefused() {
        CountingMemo memo = new CountingMemo();

        assertThat(Fibonacci.fib(92, memo)).isEqualTo(7_540_113_804_746_346_429L);
        assertThatThrownBy(() -> Fibonacci.fib(93, memo)).isInstanceOf(ArithmeticException.class);
    }
}
