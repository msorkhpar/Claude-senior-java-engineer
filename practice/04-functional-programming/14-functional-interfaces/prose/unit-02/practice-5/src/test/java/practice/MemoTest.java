package practice;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoTest {

    @Test
    void computesOnceAndServesTheCache() {
        int[] calls = {0};
        Supplier<Object> memo = Memo.memoize(() -> {
            calls[0]++;
            return new Object();
        });

        Object first = memo.get();
        assertThat(memo.get()).isSameAs(first);
        assertThat(memo.get()).isSameAs(first);
        assertThat(calls[0]).isEqualTo(1);
    }

    @Test
    void nothingRunsBeforeTheFirstGet() {
        int[] calls = {0};
        Supplier<String> memo = Memo.memoize(() -> {
            calls[0]++;
            return "value";
        });
        assertThat(calls[0]).isZero();

        assertThat(memo.get()).isEqualTo("value");
        assertThat(calls[0]).isEqualTo(1);
    }

    @Test
    void aNullResultIsCachedToo() {
        int[] calls = {0};
        Supplier<String> memo = Memo.memoize(() -> {
            calls[0]++;
            return null;
        });

        assertThat(memo.get()).isNull();
        assertThat(memo.get()).isNull();
        assertThat(calls[0]).isEqualTo(1);
    }

    @Test
    void aFailureIsNotCached() {
        int[] calls = {0};
        Supplier<String> memo = Memo.memoize(() -> {
            calls[0]++;
            if (calls[0] == 1) {
                throw new IllegalStateException("not ready");
            }
            return "ready";
        });

        assertThatThrownBy(memo::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("not ready");
        assertThat(memo.get()).isEqualTo("ready");
        assertThat(memo.get()).isEqualTo("ready");
        assertThat(calls[0]).isEqualTo(2);
    }
}
