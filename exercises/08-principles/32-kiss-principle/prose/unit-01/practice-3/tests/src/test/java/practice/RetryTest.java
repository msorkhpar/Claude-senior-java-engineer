package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class RetryTest {

    /** An action that fails with "fail 1", "fail 2", ... until its call number reaches succeedOn. */
    private static Supplier<String> failingUntil(int succeedOn, int[] calls) {
        return () -> {
            calls[0]++;
            if (calls[0] < succeedOn) {
                throw new IllegalStateException("fail " + calls[0]);
            }
            return "success on " + calls[0];
        };
    }

    @Test
    void returnsTheFirstSuccess() {
        int[] calls = {0};
        assertThat(Retry.executeWithRetry(failingUntil(1, calls), 3)).isEqualTo("success on 1");
        assertThat(calls[0]).isEqualTo(1);
        int[] again = {0};
        assertThat(Retry.executeWithRetry(failingUntil(2, again), 3)).isEqualTo("success on 2");
        assertThat(again[0]).isEqualTo(2);
        int[] nulls = {0};
        assertThat(Retry.<String>executeWithRetry(() -> {
            nulls[0]++;
            return null;
        }, 3)).isNull();
        assertThat(nulls[0]).isEqualTo(1);
    }

    @Test
    void succeedsOnTheLastAllowedAttempt() {
        int[] calls = {0};
        assertThat(Retry.executeWithRetry(failingUntil(3, calls), 3)).isEqualTo("success on 3");
        assertThat(calls[0]).isEqualTo(3);
    }

    @Test
    void rethrowsTheLastFailure() {
        int[] calls = {0};
        assertThatThrownBy(() -> Retry.executeWithRetry(failingUntil(100, calls), 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fail 3");
        assertThat(calls[0]).isEqualTo(3);
        List<RuntimeException> thrown = new ArrayList<>();
        Supplier<String> action = () -> {
            RuntimeException e = new IllegalStateException("attempt " + (thrown.size() + 1));
            thrown.add(e);
            throw e;
        };
        Throwable last = catchThrowable(() -> Retry.executeWithRetry(action, 2));
        assertThat(thrown).hasSize(2);
        assertThat(last).isSameAs(thrown.get(1));
    }

    @Test
    void anErrorIsNotRetried() {
        int[] calls = {0};
        AssertionError broken = new AssertionError("broken invariant");
        Supplier<String> action = () -> {
            calls[0]++;
            throw broken;
        };
        Throwable thrown = catchThrowable(() -> Retry.executeWithRetry(action, 3));
        assertThat(thrown).isSameAs(broken);
        assertThat(calls[0]).isEqualTo(1);
    }

    @Test
    void aNonPositiveLimitIsRefused() {
        int[] calls = {0};
        assertThatThrownBy(() -> Retry.executeWithRetry(failingUntil(1, calls), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Retry.executeWithRetry(failingUntil(1, calls), -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(calls[0]).isZero();
    }
}
