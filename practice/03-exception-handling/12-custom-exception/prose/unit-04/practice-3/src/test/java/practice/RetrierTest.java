package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class RetrierTest {

    @Test
    void retriesUntilItSucceeds() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = Retrier.withRetry(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new TransientException("try " + calls.get());
            }
            return "done";
        }, 3);
        assertThat(result).isEqualTo("done");
        assertThat(calls).hasValue(3);
        assertThat(Retrier.withRetry(() -> 7, 1)).isEqualTo(7);
    }

    @Test
    void aBugIsNotRetried() {
        AtomicInteger calls = new AtomicInteger();
        IllegalStateException bug = new IllegalStateException("bug");
        Throwable thrown = catchThrowable(() -> Retrier.withRetry(() -> {
            calls.incrementAndGet();
            throw bug;
        }, 5));
        assertThat(thrown).isSameAs(bug);
        assertThat(calls).hasValue(1);
    }

    @Test
    void theLastFailureIsRethrownAfterTheLastAttempt() {
        AtomicInteger calls = new AtomicInteger();
        Throwable thrown = catchThrowable(() -> Retrier.withRetry(() -> {
            throw new TransientException("try " + calls.incrementAndGet());
        }, 3));
        assertThat(thrown).isInstanceOf(TransientException.class).hasMessage("try 3");
        assertThat(calls).hasValue(3);
    }

    @Test
    void atLeastOneAttemptIsRequired() {
        AtomicInteger calls = new AtomicInteger();
        assertThatThrownBy(() -> Retrier.withRetry(() -> calls.incrementAndGet(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("attempts must be at least 1: 0");
        assertThat(calls).hasValue(0);
    }

    @Test
    void theLastFailureIsRethrownAsItself() {
        List<TransientException> made = new ArrayList<>();
        Throwable thrown = catchThrowable(() -> Retrier.withRetry(() -> {
            TransientException e = new TransientException("try " + (made.size() + 1));
            made.add(e);
            throw e;
        }, 2));
        assertThat(made).hasSize(2);
        assertThat(thrown).isSameAs(made.get(1));
    }
}
