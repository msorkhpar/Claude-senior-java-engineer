package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

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

        List<String> subclassLog = new ArrayList<>();
        assertThat(Flow.run(() -> {
            throw new CancellationException("stop");
        }, subclassLog)).isEqualTo(-1);
        assertThat(subclassLog).containsExactly("try", "catch stop", "finally");

        List<String> noMessageLog = new ArrayList<>();
        assertThat(Flow.run(() -> {
            throw new IllegalStateException();
        }, noMessageLog)).isEqualTo(-1);
        assertThat(noMessageLog).containsExactly("try", "catch null", "finally");
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

        List<String> errorLog = new ArrayList<>();
        Error fatal = new Error("fatal");
        assertThatThrownBy(() -> Flow.run(() -> {
            throw fatal;
        }, errorLog)).isSameAs(fatal);
        assertThat(errorLog).containsExactly("try", "finally");

        List<String> checkedLog = new ArrayList<>();
        Exception checked = new Exception("checked");
        assertThatThrownBy(() -> Flow.run(() -> {
            throw FlowTest.<RuntimeException>sneaky(checked);
        }, checkedLog)).isSameAs(checked);
        assertThat(checkedLog).containsExactly("try", "finally");
    }

    /** Throws any throwable past the compiler's checks. */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E {
        throw (E) t;
    }
}
