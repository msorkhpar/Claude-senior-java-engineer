package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A channel that records every call in a shared log, and can be told to fail reading or closing. */
interface Recording extends Transfer.Channel {
    String name();

    List<String> log();

    String content();

    boolean failRead();

    boolean failClose();

    @Override
    default String read() throws IOException {
        log().add("read " + name());
        if (failRead()) {
            throw new IOException("read failed");
        }
        return content();
    }

    @Override
    default void write(String text) {
        log().add("write " + name() + " " + text);
    }

    @Override
    default void close() throws IOException {
        log().add("close " + name());
        if (failClose()) {
            throw new IOException("close " + name() + " failed");
        }
    }
}

record Tracked(String name, List<String> log, String content, boolean failRead, boolean failClose)
        implements Recording {

    static Tracked ok(String name, List<String> log) {
        return new Tracked(name, log, "data", false, false);
    }
}

class TransferTest {

    private static Transfer.Opener opener(List<String> log) {
        return name -> {
            log.add("open " + name);
            return Tracked.ok(name, log);
        };
    }

    @Test
    void copiesInToOut() throws IOException {
        List<String> log = new ArrayList<>();
        Transfer.copy(opener(log));
        assertThat(log).contains("write out data");
        assertThat(log).filteredOn(line -> line.startsWith("close "))
                .containsExactlyInAnyOrder("close in", "close out");
    }

    @Test
    void closesInReverseOrder() throws IOException {
        List<String> log = new ArrayList<>();
        Transfer.copy(opener(log));
        assertThat(log).containsExactly(
                "open in", "open out", "read in", "write out data", "close out", "close in");
    }

    @Test
    void aFailedSecondOpenClosesTheFirst() {
        List<String> log = new ArrayList<>();
        IOException noOut = new IOException("cannot open out");
        Transfer.Opener failing = name -> {
            if (name.equals("out")) {
                throw noOut;
            }
            log.add("open " + name);
            return Tracked.ok(name, log);
        };
        assertThatThrownBy(() -> Transfer.copy(failing)).isSameAs(noOut);
        assertThat(log).containsExactly("open in", "close in");

        List<String> brokenLog = new ArrayList<>();
        IllegalStateException broken = new IllegalStateException("opener broken");
        Transfer.Opener breaking = name -> {
            if (name.equals("out")) {
                throw broken;
            }
            brokenLog.add("open " + name);
            return Tracked.ok(name, brokenLog);
        };
        assertThatThrownBy(() -> Transfer.copy(breaking)).isSameAs(broken);
        assertThat(brokenLog).containsExactly("open in", "close in");
    }

    @Test
    void aReadFailureIsKeptWhenClosingAlsoFails() {
        List<String> log = new ArrayList<>();
        Transfer.Opener opener = name -> {
            log.add("open " + name);
            return name.equals("in")
                    ? new Tracked(name, log, "data", true, false)
                    : new Tracked(name, log, "data", false, true);
        };
        assertThatThrownBy(() -> Transfer.copy(opener))
                .isInstanceOf(IOException.class)
                .hasMessage("read failed")
                .satisfies(t -> assertThat(t.getSuppressed())
                        .singleElement()
                        .satisfies(s -> assertThat(s).hasMessage("close out failed")));
        assertThat(log).containsExactly("open in", "open out", "read in", "close out", "close in");
    }
}
