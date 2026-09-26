package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class UncheckedTest {

    @Test
    void returnsTheValue() {
        assertThat(Unchecked.get(() -> 42)).isEqualTo(42);
        assertThat(Unchecked.get(() -> "text")).isEqualTo("text");
    }

    @Test
    void aCheckedFailureIsTranslated() {
        SQLException sql = new SQLException("x");
        Throwable thrown = catchThrowable(() -> Unchecked.get(() -> {
            throw sql;
        }));
        assertThat(thrown).isExactlyInstanceOf(RuntimeException.class).hasMessage("Translated exception");
        assertThat(thrown.getCause()).isSameAs(sql);
    }

    @Test
    void anIoFailureBecomesUncheckedIOException() {
        IOException io = new IOException("disk");
        Throwable thrown = catchThrowable(() -> Unchecked.get(() -> {
            throw io;
        }));
        assertThat(thrown).isExactlyInstanceOf(UncheckedIOException.class).hasMessage("disk");
        assertThat(thrown.getCause()).isSameAs(io);
    }

    @Test
    void anUncheckedFailurePassesThrough() {
        assertThat(Unchecked.get(() -> 1)).isEqualTo(1);
        IllegalArgumentException bad = new IllegalArgumentException("bad");
        Throwable thrown = catchThrowable(() -> Unchecked.get(() -> {
            throw bad;
        }));
        assertThat(thrown).isSameAs(bad);
    }
}
