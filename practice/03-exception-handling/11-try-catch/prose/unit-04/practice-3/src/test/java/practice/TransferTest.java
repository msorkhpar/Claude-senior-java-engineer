package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A channel that records every call in a shared log. */
interface Recording extends Transfer.Channel {
    String name();

    List<String> log();

    String content();

    @Override
    default String read() {
        log().add("read " + name());
        return content();
    }

    @Override
    default void write(String text) {
        log().add("write " + name() + " " + text);
    }

    @Override
    default void close() {
        log().add("close " + name());
    }
}

record Tracked(String name, List<String> log, String content) implements Recording {
}

class TransferTest {

    private static Transfer.Opener opener(List<String> log) {
        return name -> {
            log.add("open " + name);
            return new Tracked(name, log, "data");
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
            return new Tracked(name, log, "data");
        };
        assertThatThrownBy(() -> Transfer.copy(failing)).isSameAs(noOut);
        assertThat(log).containsExactly("open in", "close in");
    }
}
