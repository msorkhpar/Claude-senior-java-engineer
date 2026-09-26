package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A channel whose write and close can be told to fail; it records what happens. */
interface Scripted extends LogWriter.Channel {
    List<String> log();

    boolean failWrite();

    boolean failClose();

    @Override
    default void write(String text) throws IOException {
        if (failWrite()) {
            throw new IOException("write failed");
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

record Script(List<String> log, boolean failWrite, boolean failClose) implements Scripted {
}

class LogWriterTest {

    private static LogWriter.Opener opening(Script channel) {
        return name -> channel;
    }

    @Test
    void reportsOk() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, false, false)), "hello")).isEqualTo("ok");
        assertThat(log).containsExactly("write hello", "close");
    }

    @Test
    void reportsAWriteFailure() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, true, false)), "hello"))
                .isEqualTo("failed: write failed");
        assertThat(log).containsExactly("close");
        assertThat(LogWriter.write(name -> {
            throw new IOException("cannot open");
        }, "hello")).isEqualTo("failed: cannot open");
    }

    @Test
    void aCloseFailureAfterAWriteFailureIsReportedAsSuppressed() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, true, true)), "hello"))
                .isEqualTo("failed: write failed; suppressed: close failed");
    }

    @Test
    void aCloseFailureAloneIsReported() {
        List<String> log = new ArrayList<>();
        assertThat(LogWriter.write(opening(new Script(log, false, true)), "hello"))
                .isEqualTo("failed: close failed");
        assertThat(log).containsExactly("write hello", "close");
    }
}
