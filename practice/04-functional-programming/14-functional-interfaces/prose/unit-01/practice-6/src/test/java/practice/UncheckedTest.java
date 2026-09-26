package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UncheckedTest {

    @Test
    void runsTheActionForEachItem() {
        List<String> seen = new ArrayList<>();
        List.of("a", "b", "c").forEach(Unchecked.unchecked(s -> seen.add(s)));
        assertThat(seen).containsExactly("a", "b", "c");
    }

    @Test
    void anIoFailureBecomesUncheckedWithItsCause() {
        IOException disk = new IOException("disk");
        Unchecked.IoAction<String> failing = s -> {
            throw disk;
        };

        assertThatThrownBy(() -> Unchecked.unchecked(failing).accept("a"))
                .isInstanceOf(UncheckedIOException.class)
                .cause().isSameAs(disk);
    }

    @Test
    void runtimeFailuresPassThroughUnwrapped() {
        Unchecked.IoAction<String> failing = s -> {
            throw new IllegalArgumentException("bad " + s);
        };

        assertThatThrownBy(() -> Unchecked.unchecked(failing).accept("a"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("bad a");
    }

    @Test
    void aFailureStopsTheLoop() {
        List<String> seen = new ArrayList<>();
        Unchecked.IoAction<String> action = s -> {
            if (s.equals("bad")) {
                throw new IOException("cannot " + s);
            }
            seen.add(s);
        };

        assertThatThrownBy(() -> List.of("a", "bad", "c").forEach(Unchecked.unchecked(action)))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("cannot bad");
        assertThat(seen).containsExactly("a");
    }
}
