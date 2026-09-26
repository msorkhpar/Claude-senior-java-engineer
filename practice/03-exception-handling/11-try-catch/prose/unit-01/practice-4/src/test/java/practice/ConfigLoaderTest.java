package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;

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
    }
}
