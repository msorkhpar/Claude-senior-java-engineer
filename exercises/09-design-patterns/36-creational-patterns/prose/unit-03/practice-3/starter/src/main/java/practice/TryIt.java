package practice;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
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
            LabelRequests requests = new LabelRequests("https://api.carrier.example.com/v2/labels", "demo-token");
            requests.label("dock4-000101", "{\"parcel\":\"QA-1\"}");
            java.net.http.HttpRequest label = requests.label("dock4-000102", "{\"parcel\":\"QA-2\",\"weightGrams\":920}");
            System.out.println("label: " + label.method() + " " + label.uri());
            System.out.println("timeout: " + label.timeout().orElse(null));
            System.out.println("X-Request-Id: " + label.headers().allValues("X-Request-Id"));
            System.out.println("body bytes: " + label.bodyPublisher().orElseThrow().contentLength());
            java.net.http.HttpRequest status = requests.status("LBL-30311");
            System.out.println("status: " + status.method() + " " + status.uri() + ", X-Request-Id " + status.headers().allValues("X-Request-Id"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
