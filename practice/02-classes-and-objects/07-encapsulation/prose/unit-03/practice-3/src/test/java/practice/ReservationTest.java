package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ReservationTest {

    private static byte[] write(Reservation r) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(r);
        }
        return bytes.toByteArray();
    }

    private static Object read(byte[] stream) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(stream))) {
            return in.readObject();
        }
    }

    /**
     * A stream for a reservation the constructor would refuse: a valid one written out, with its
     * guests and nights (stored side by side, guests first) overwritten.
     */
    private static byte[] streamWith(int guests, int nights) throws IOException {
        byte[] stream = write(new Reservation("Suite 12", 3, 17));
        byte[] pair = ByteBuffer.allocate(8).putInt(3).putInt(17).array();
        int at = -1;
        for (int i = stream.length - pair.length; i >= 0 && at < 0; i--) {
            boolean match = true;
            for (int j = 0; j < pair.length && match; j++) match = stream[i + j] == pair[j];
            if (match) at = i;
        }
        assertThat(at).as("guests and nights in the stream").isNotNegative();
        ByteBuffer.wrap(stream).putInt(at, guests).putInt(at + 4, nights);
        return stream;
    }

    @Test
    void aReservationComesBackAsItWasWritten() throws Exception {
        for (Reservation r : List.of(new Reservation("Room 204", 2, 5), new Reservation("Loft 3", 4, 28),
                new Reservation("Room 7", 1, 1))) {
            Reservation back = (Reservation) read(write(r));

            assertThat(back).isNotSameAs(r);
            assertThat(back.room()).isEqualTo(r.room());
            assertThat(back.guests()).isEqualTo(r.guests());
            assertThat(back.nights()).isEqualTo(r.nights());
        }
    }

    @Test
    void aStreamTheConstructorWouldRefuseGivesNoReservation() throws Exception {
        for (int[] bad : new int[][] {{0, 17}, {9, 17}, {3, 0}, {3, -4}, {3, 400}}) {
            Throwable thrown = catchThrowable(() -> {
                Object o = read(streamWith(bad[0], bad[1]));
                throw new AssertionError("restored guests=" + ((Reservation) o).guests()
                        + " nights=" + ((Reservation) o).nights());
            });

            assertThat(thrown).as("guests=%d nights=%d", bad[0], bad[1])
                    .isInstanceOfAny(IOException.class, IllegalArgumentException.class);
        }
    }

    @Test
    void aBadStreamIsReportedAsABadStream() throws Exception {
        Throwable thrown = catchThrowable(() -> read(streamWith(3, 0)));

        assertThat(thrown).isInstanceOf(IOException.class);
    }
}
