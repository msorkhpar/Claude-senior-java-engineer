package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class AuditTest {

    @Test
    void logsSuccessAndFailure() throws Exception {
        List<String> log = new ArrayList<>();
        Audit.runLogged(() -> { }, log);
        assertThat(log).containsExactly("ok");

        List<String> failed = new ArrayList<>();
        Throwable thrown = catchThrowable(() -> Audit.runLogged(() -> {
            throw new IOException("disk full");
        }, failed));
        assertThat(thrown).isInstanceOf(IOException.class).hasMessage("disk full");
        assertThat(failed).containsExactly("failed: disk full");
    }

    @Test
    void theSameExceptionIsRethrown() {
        IOException io = new IOException("disk full");
        StackTraceElement[] original = io.getStackTrace();
        List<String> log = new ArrayList<>();
        Throwable thrown = catchThrowable(() -> Audit.runLogged(() -> {
            throw io;
        }, log));
        assertThat(thrown).isSameAs(io);
        assertThat(thrown.getStackTrace()).containsExactly(original);
        assertThat(log).containsExactly("failed: disk full");
    }

    @Test
    void anUncheckedFailureIsLoggedToo() {
        IllegalStateException bug = new IllegalStateException("bug");
        List<String> log = new ArrayList<>();
        Throwable thrown = catchThrowable(() -> Audit.runLogged(() -> {
            throw bug;
        }, log));
        assertThat(thrown).isSameAs(bug);
        assertThat(log).containsExactly("failed: bug");
    }

    @Test
    void declaresOnlyIOException() throws Exception {
        List<String> log = new ArrayList<>();
        Audit.runLogged(() -> { }, log);
        assertThat(log).containsExactly("ok");
        assertThat(Audit.class.getMethod("runLogged", Step.class, List.class).getExceptionTypes())
                .containsExactly(IOException.class);
    }
}
