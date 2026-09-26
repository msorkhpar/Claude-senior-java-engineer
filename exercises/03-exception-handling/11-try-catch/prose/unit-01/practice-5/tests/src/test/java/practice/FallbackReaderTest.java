package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FallbackReaderTest {

    private static final FallbackReader.Source UNTOUCHED = name -> {
        throw new AssertionError("the backup must not be read");
    };

    @Test
    void readsFromThePrimary() throws IOException {
        assertThat(FallbackReader.read(name -> "a:" + name, UNTOUCHED, "doc")).isEqualTo("a:doc");
    }

    @Test
    void fallsBackWhenThePrimaryFails() throws IOException {
        FallbackReader.Source down = name -> {
            throw new IOException("primary down");
        };
        assertThat(FallbackReader.read(down, name -> "b:" + name, "doc")).isEqualTo("b:doc");
    }

    @Test
    void whenBothFailThePrimaryFailureIsNotLost() {
        IOException primaryDown = new IOException("primary down");
        IOException backupDown = new IOException("backup down");
        assertThatThrownBy(() -> FallbackReader.read(name -> {
            throw primaryDown;
        }, name -> {
            throw backupDown;
        }, "doc"))
                .isSameAs(backupDown)
                .satisfies(t -> assertThat(t.getSuppressed()).containsExactly(primaryDown));
    }

    @Test
    void anUncheckedPrimaryFailureIsNotRetried() {
        IllegalStateException bug = new IllegalStateException("primary not configured");
        assertThatThrownBy(() -> FallbackReader.read(name -> {
            throw bug;
        }, name -> "b:" + name, "doc")).isSameAs(bug);
    }

    @Test
    void whenBothFailTheBackupFailureIsThrown() {
        IOException primaryDown = new IOException("primary down");
        IOException backupDown = new IOException("backup down");
        assertThatThrownBy(() -> FallbackReader.read(name -> {
            throw primaryDown;
        }, name -> {
            throw backupDown;
        }, "doc"))
                .isSameAs(backupDown)
                .hasMessage("backup down");
        assertThat(primaryDown.getSuppressed()).isEmpty();
        assertThat(backupDown.getCause()).isNull();
    }

    @Test
    void aFailingPrimaryIsReadOnce() throws IOException {
        AtomicInteger primaryReads = new AtomicInteger();
        FallbackReader.Source flaky = name -> {
            if (primaryReads.incrementAndGet() == 1) {
                throw new IOException("primary busy");
            }
            return "a:" + name;
        };
        assertThat(FallbackReader.read(flaky, name -> "b:" + name, "doc")).isEqualTo("b:doc");
        assertThat(primaryReads).hasValue(1);
    }
}
