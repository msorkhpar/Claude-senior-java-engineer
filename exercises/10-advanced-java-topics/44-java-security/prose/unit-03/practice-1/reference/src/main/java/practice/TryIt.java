package practice;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            SecretKey key = generator.generateKey();
            ColumnCipher service = new ColumnCipher(key, new SecureRandom());
            ColumnCipher export = new ColumnCipher(key, new SecureRandom());
            byte[] first = service.seal("DE-811 907 980");
            byte[] second = service.seal("DE-811 907 980");
            System.out.println("stored length: " + first.length + " bytes");
            System.out.println("same IV twice: " + HexFormat.of().formatHex(first, 0, 12).equals(HexFormat.of().formatHex(second, 0, 12)));
            System.out.println("export job opens: " + export.open(first));
            byte[] edited = first.clone();
            edited[edited.length - 3] ^= 0x01;
            try {
                System.out.println("edited row opens as: " + export.open(edited));
            } catch (GeneralSecurityException refused) {
                System.out.println("edited row refused: " + refused.getClass().getSimpleName());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
