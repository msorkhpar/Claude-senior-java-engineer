package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A channel whose write can fail with a given exception and whose close can fail; it records what happens. */
interface Scripted extends LogWriter.Channel {
    List<String> log();

    Exception writeFailure();

    boolean failClose();

    @Override
    default void write(String text) throws IOException {
        if (writeFailure() instanceof IOException io) {
            throw io;
        }
        if (writeFailure() instanceof RuntimeException unchecked) {
            throw unchecked;
        }
        log().add("write " + text);
    }

    @Override
    default void close() throws IOException {
        log().add("close");
        if (failClose()) {
            throw new IOException("close failed");
        }
    }
}

record Script(List<String> log, Exception writeFailure, boolean failClose) implements Scripted {
}

class LogWriterTest {

    /** Hands out the channel only under the name "log". */
    private static LogWriter.Opener opening(Script channel) {
        return name -> {
            if (!name.equals("log")) {
                throw new IOException("no channel named " + name);
            }
            return channel;
        };
    }

    private static IOException writeFailed() {
        return new IOException("write failed");
    }

    @Test
    void reportsOk() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, null, false)), "hello")).isEqualTo("ok");
        assertThat(log).containsExactly("write hello", "close");
    }

    @Test
    void reportsAWriteFailure() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, writeFailed(), false)), "hello"))
                .isEqualTo("failed: write failed");
        assertThat(log).containsExactly("close");
        assertThat(LogWriter.write(name -> {
            throw new IOException("cannot open");
        }, "hello")).isEqualTo("failed: cannot open");
    }

    @Test
    void aCloseFailureAfterAWriteFailureIsReportedAsSuppressed() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, writeFailed(), true)), "hello"))
                .isEqualTo("failed: write failed; suppressed: close failed");

        IOException retried = writeFailed();
        retried.addSuppressed(new IOException("retry failed"));
        assertThat(LogWriter.write(opening(new Script(new ArrayList<>(), retried, true)), "hello"))
                .isEqualTo("failed: write failed; suppressed: retry failed; suppressed: close failed");
    }

    @Test
    void aCloseFailureAloneIsReported() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, null, true)), "hello"))
                .isEqualTo("failed: close failed");
        assertThat(log).containsExactly("write hello", "close");
    }

    @Test
    void anUncheckedFailureIsNotReported() {
        List<String> log = new ArrayList<>();
        IllegalStateException bug = new IllegalStateException("channel bug");
        assertThatThrownBy(() -> LogWriter.write(opening(new Script(log, bug, false)), "hello")).isSameAs(bug);
        assertThat(log).containsExactly("close");
    }
}
