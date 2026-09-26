package practice;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class CausesTest {

    @Test
    void findsAWrappedCause() {
        IOException io = new IOException("disk");
        RuntimeException middle = new RuntimeException("wrapped", io);
        IllegalStateException outer = new IllegalStateException("load failed", middle);
        assertThat(Causes.find(outer, IOException.class)).containsSame(io);
        assertThat(Causes.find(outer, ArithmeticException.class)).isEmpty();
    }

    @Test
    void emptyWhenNoCauseMatches() {
        IllegalStateException outer = new IllegalStateException("load failed",
                new RuntimeException("wrapped", new IOException("disk")));
        assertThat(Causes.find(outer, SQLException.class)).isEmpty();
        assertThat(Causes.find(new IOException("alone"), SQLException.class)).isEmpty();
    }

    @Test
    void theThrowableItselfCounts() {
        IOException io = new IOException("disk");
        assertThat(Causes.find(io, IOException.class)).containsSame(io);
        IllegalStateException outer = new IllegalStateException("load failed", new RuntimeException("wrapped"));
        assertThat(Causes.find(outer, IllegalStateException.class)).containsSame(outer);
    }

    @Test
    void aSubclassMatches() {
        FileNotFoundException missing = new FileNotFoundException("a.txt");
        IllegalStateException outer = new IllegalStateException("load failed", missing);
        assertThat(Causes.find(outer, IOException.class)).containsSame(missing);
        assertThat(Causes.find(outer, RuntimeException.class)).containsSame(outer);
    }
}
