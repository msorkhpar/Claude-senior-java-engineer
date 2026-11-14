package practice;

import java.util.concurrent.atomic.AtomicReference;
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
            TlsCredentials credentials = new TlsCredentials("cert:api-2026-01", "key:api-2026-01");
            System.out.println("before renewal: " + credentials.current());
            Thread reload = new Thread(() -> credentials.reload(new TlsCredentials.Source() {
                public String certificate() { return "cert:api-2026-07"; }
                public String privateKey() {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "key:api-2026-07";
                }
            }), "reload");
            reload.setDaemon(true);
            reload.start();
            Thread.sleep(100);
            AtomicReference<Object> during = new AtomicReference<>("no answer within 1 s");
            Thread handshake = new Thread(() -> during.set(credentials.current()), "handshake");
            handshake.setDaemon(true);
            handshake.start();
            handshake.join(1_000);
            System.out.println("handshake while the vault reads the key: " + during.get());
            reload.join(2_000);
            System.out.println("after renewal: " + credentials.current());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
