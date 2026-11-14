package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            Reservation stay = new Reservation("Garden 5", 2, 6);
            Reservation back = (Reservation) read(write(stay));
            System.out.println("read back: " + back.room() + ", " + back.guests() + " guests, " + back.nights() + " nights");
            System.out.println("price at 12,000 a night: " + back.priceCents(12_000));
            byte[] tampered = write(stay);
            int at = indexOf(tampered, ByteBuffer.allocate(8).putInt(2).putInt(6).array());
            ByteBuffer.wrap(tampered).putInt(at, 7).putInt(at + 4, 6);
            try {
                Reservation bad = (Reservation) read(tampered);
                System.out.println("stream with 7 guests: read back with " + bad.guests() + " guests");
            } catch (IOException unreadable) {
                System.out.println("stream with 7 guests: unreadable, " + unreadable);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

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

    private static int indexOf(byte[] stream, byte[] pair) {
        for (int i = stream.length - pair.length; i >= 0; i--) {
            if (java.util.Arrays.equals(stream, i, i + pair.length, pair, 0, pair.length)) {
                return i;
            }
        }
        throw new IllegalStateException("guests and nights not found in the stream");
    }
}
