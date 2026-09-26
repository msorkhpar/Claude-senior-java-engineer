package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A resource that records its close in a shared log, and can be told to fail closing. */
interface Logged extends Traced.Resource {
    List<String> log();

    boolean failClose();

    @Override
    default void close() throws IOException {
        log().add("close r");
        if (failClose()) {
            throw new IOException("close failed");
        }
    }
}

record LoggedResource(List<String> log, boolean failClose) implements Logged {
}

class TracedTest {

    private static Traced.Opener opener(List<String> log, boolean failClose) {
        return name -> {
            log.add("open " + name);
            return new LoggedResource(log, failClose);
        };
    }

    @Test
    void aCleanRunClosesThenRunsFinally() {
        List<String> log = new ArrayList<>();
        Traced.run(opener(log, false), () -> log.add("working"), log);
        assertThat(log).containsExactly("open r", "working", "body done", "close r", "finally");
    }

    @Test
    void theCatchRunsAfterTheResourceIsClosed() {
        List<String> log = new ArrayList<>();
        Traced.run(opener(log, false), () -> {
            throw new IOException("boom");
        }, log);
        assertThat(log).containsExactly("open r", "close r", "caught boom", "finally");
    }

    @Test
    void aCloseFailureIsCaughtByTheSameCatch() {
        List<String> log = new ArrayList<>();
        Traced.run(opener(log, true), () -> log.add("working"), log);
        assertThat(log).containsExactly("open r", "working", "body done", "close r", "caught close failed", "finally");
    }
}
