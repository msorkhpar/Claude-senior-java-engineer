package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class ConfigLoaderTest {

    @Test
    void returnsWhatTheSourceReads() throws ConfigException {
        assertThat(ConfigLoader.load(name -> "port=80", "app.conf")).isEqualTo("port=80");
        assertThat(ConfigLoader.load(name -> "name=" + name, "db.conf")).isEqualTo("name=db.conf");
    }

    @Test
    void anIoFailureBecomesAConfigException() {
        assertThatThrownBy(() -> ConfigLoader.load(name -> {
            throw new IOException("disk gone");
        }, "app.conf"))
                .isInstanceOf(ConfigException.class)
                .hasMessage("Error processing file app.conf");
    }

    @Test
    void theIoFailureIsKeptAsTheCause() {
        IOException failure = new IOException("disk gone");
        Throwable thrown = catchThrowable(() -> ConfigLoader.load(name -> {
            throw failure;
        }, "app.conf"));
        assertThat(thrown).isInstanceOf(ConfigException.class);
        assertThat(thrown.getCause()).isSameAs(failure);
    }

    @Test
    void anUncheckedFailureIsNotWrapped() {
        IllegalStateException bug = new IllegalStateException("source not opened");
        assertThatThrownBy(() -> ConfigLoader.load(name -> {
            throw bug;
        }, "app.conf")).isSameAs(bug);
        IllegalArgumentException badName = new IllegalArgumentException("bad name");
        assertThatThrownBy(() -> ConfigLoader.load(name -> {
            throw badName;
        }, "app.conf")).isSameAs(badName);
        UncheckedIOException unchecked = new UncheckedIOException(new IOException("wrapped elsewhere"));
        assertThatThrownBy(() -> ConfigLoader.load(name -> {
            throw unchecked;
        }, "app.conf")).isSameAs(unchecked);
    }

    @Test
    void theSourceIsReadOnce() {
        AtomicInteger reads = new AtomicInteger();
        assertThatThrownBy(() -> ConfigLoader.load(name -> {
            if (reads.incrementAndGet() == 1) {
                throw new IOException("disk busy");
            }
            return "port=80";
        }, "app.conf"))
                .isInstanceOf(ConfigException.class)
                .hasMessage("Error processing file app.conf");
        assertThat(reads).hasValue(1);
    }
}
