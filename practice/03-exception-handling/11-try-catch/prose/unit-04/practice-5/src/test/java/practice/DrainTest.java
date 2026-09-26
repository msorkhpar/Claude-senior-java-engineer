package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A line source over a queue; the entries "!fail" and "!broken" make readLine throw. It records its close. */
interface Queued extends Drain.LineSource {
    Deque<String> lines();

    List<String> log();

    boolean failClose();

    @Override
    default String readLine() throws IOException {
        String next = lines().poll();
        if ("!fail".equals(next)) {
            throw new IOException("read failed");
        }
        if ("!broken".equals(next)) {
            throw new IllegalStateException("source broken");
        }
        return next;
    }

    @Override
    default void close() throws IOException {
        log().add("close");
        if (failClose()) {
            throw new IOException("close failed");
        }
    }
}

record Lines(Deque<String> lines, List<String> log, boolean failClose) implements Queued {

    static Lines of(List<String> log, boolean failClose, String... lines) {
        return new Lines(new ArrayDeque<>(List.of(lines)), log, failClose);
    }
}

class DrainTest {

    @Test
    void readsEveryLineAndCloses() throws IOException {
        List<String> log = new ArrayList<>();
        assertThat(Drain.readAll(Lines.of(log, false, "a", "b"))).containsExactly("a", "b");
        assertThat(log).containsExactly("close");
        assertThat(Drain.readAll(Lines.of(log, false))).isEmpty();
        assertThat(log).containsExactly("close", "close");
        assertThat(Drain.readAll(Lines.of(log, false, "a", "", "b"))).containsExactly("a", "", "b");
    }

    @Test
    void aReadFailureStillCloses() {
        List<String> log = new ArrayList<>();
        assertThatThrownBy(() -> Drain.readAll(Lines.of(log, false, "a", "!fail", "c")))
                .isInstanceOf(IOException.class)
                .hasMessage("read failed");
        assertThat(log).containsExactly("close");

        List<String> brokenLog = new ArrayList<>();
        assertThatThrownBy(() -> Drain.readAll(Lines.of(brokenLog, false, "a", "!broken")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("source broken");
        assertThat(brokenLog).containsExactly("close");
    }

    @Test
    void aCloseFailureReachesTheCaller() {
        List<String> log = new ArrayList<>();
        assertThatThrownBy(() -> Drain.readAll(Lines.of(log, true, "a")))
                .isInstanceOf(IOException.class)
                .hasMessage("close failed");
        assertThat(log).containsExactly("close");
    }

    @Test
    void aReadFailureIsKeptWhenTheCloseAlsoFails() {
        List<String> log = new ArrayList<>();
        assertThatThrownBy(() -> Drain.readAll(Lines.of(log, true, "a", "!fail")))
                .isInstanceOf(IOException.class)
                .hasMessage("read failed")
                .satisfies(t -> assertThat(t.getSuppressed())
                        .singleElement()
                        .satisfies(s -> assertThat(s).hasMessage("close failed")));
        assertThat(log).containsExactly("close");
    }
}
