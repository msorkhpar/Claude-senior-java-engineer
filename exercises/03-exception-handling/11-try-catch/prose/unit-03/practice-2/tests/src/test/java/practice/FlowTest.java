package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowTest {

    @Test
    void aFailureRunsCatchThenFinally() {
        List<String> log = new ArrayList<>();
        int result = Flow.run(() -> {
            throw new IllegalStateException("boom");
        }, log);
        assertThat(result).isEqualTo(-1);
        assertThat(log).containsExactly("try", "catch boom", "finally");
    }

    @Test
    void finallyRunsWhenTheTryReturns() {
        List<String> log = new ArrayList<>();
        assertThat(Flow.run(() -> 7, log)).isEqualTo(7);
        assertThat(log).containsExactly("try", "finally");
    }

    @Test
    void finallyRunsWhenAnUncaughtExceptionLeaves() {
        List<String> log = new ArrayList<>();
        IllegalArgumentException other = new IllegalArgumentException("other");
        assertThatThrownBy(() -> Flow.run(() -> {
            throw other;
        }, log)).isSameAs(other);
        assertThat(log).containsExactly("try", "finally");
    }
}
