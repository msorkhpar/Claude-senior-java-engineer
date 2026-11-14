package practice;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
            ProxyStream quiet = new ProxyStream();
            List<String> lines = new CopyOnWriteArrayList<>();
            List<String> pages = new CopyOnWriteArrayList<>();
            LogTailer retired = new LogTailer(quiet, lines::add, e -> pages.add(e.getMessage()));
            retired.start();
            quiet.send("08:15:02 GET /login 200");
            quiet.send("08:15:03 GET /favicon.ico 404");
            for (int i = 0; i < 50 && lines.size() < 2; i++) Thread.sleep(20);
            FutureTask<Void> stop = new FutureTask<>(() -> { retired.stop(); return null; });
            Thread stopper = new Thread(stop, "admin");
            stopper.setDaemon(true);
            stopper.start();
            try {
                stop.get(3, TimeUnit.SECONDS);
                System.out.println("stop() returned on a quiet proxy: true");
            } catch (java.util.concurrent.TimeoutException stillWaiting) {
                System.out.println("stop() returned on a quiet proxy: false");
            } catch (java.util.concurrent.ExecutionException e) {
                throw e.getCause() instanceof RuntimeException r ? r : new IllegalStateException(e.getCause());
            }
            System.out.println("lines: " + lines);
            System.out.println("pages after retiring: " + pages);
            ProxyStream broken = new ProxyStream();
            new LogTailer(broken, line -> { }, e -> pages.add(e.getMessage())).start();
            broken.breakConnection();
            for (int i = 0; i < 50 && pages.isEmpty(); i++) Thread.sleep(20);
            System.out.println("pages after a broken connection: " + pages);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Behaves like a socket's stream: read() waits for the next byte, and close() makes it throw. */
    static final class ProxyStream extends InputStream {
        private final BlockingQueue<Integer> bytes = new LinkedBlockingQueue<>();
        void send(String line) { for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) bytes.add(b & 0xff); }
        void breakConnection() { bytes.add(-3); }
        @Override public void close() { bytes.add(-2); }
        @Override public int read() throws IOException {
            int b;
            try {
                b = bytes.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
            if (b == -2) { bytes.add(-2); throw new IOException("Socket closed"); }
            if (b == -3) throw new IOException("Connection reset by peer");
            return b;
        }
        @Override public int read(byte[] buf, int off, int len) throws IOException {
            if (len == 0) return 0;
            buf[off] = (byte) read();
            return 1;
        }
    }
}
