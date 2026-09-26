package practice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafeReaderTest {

    record Note(String text) implements Serializable {
    }

    static final class Envelope implements Serializable {
        final Object payload;

        Envelope(Object payload) {
            this.payload = payload;
        }
    }

    /** Stands in for a gadget class: its readObject() has a side effect. */
    static final class Gadget implements Serializable {
        static final AtomicInteger RUNS = new AtomicInteger();

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            RUNS.incrementAndGet();
        }
    }

    private static byte[] bytesOf(Object value) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(value);
        }
        return buffer.toByteArray();
    }

    @BeforeEach
    void reset() {
        Gadget.RUNS.set(0);
    }

    @Test
    void readsAllowedClassesAndRefusesOthers() throws Exception {
        Object note = SafeReader.read(bytesOf(new Note(new StringBuilder("hel").append("lo").toString())), Set.of(Note.class));

        assertThat(note).isEqualTo(new Note("hello"));
        assertThatThrownBy(() -> SafeReader.read(bytesOf(new Gadget()), Set.of(Note.class)))
                .isInstanceOf(InvalidClassException.class);
    }

    @Test
    void aRefusedClassNeverRuns() throws Exception {
        byte[] gadget = bytesOf(new Gadget());

        assertThatThrownBy(() -> SafeReader.read(gadget, Set.of(Note.class)))
                .isInstanceOf(InvalidClassException.class);

        assertThat(Gadget.RUNS.get()).isZero();
    }

    @Test
    void aRefusedClassInsideAnAllowedOneIsRefused() throws Exception {
        byte[] wrapped = bytesOf(new Envelope(new Gadget()));

        assertThatThrownBy(() -> SafeReader.read(wrapped, Set.of(Envelope.class)))
                .isInstanceOf(InvalidClassException.class);
        assertThat(Gadget.RUNS.get()).isZero();
    }
}
