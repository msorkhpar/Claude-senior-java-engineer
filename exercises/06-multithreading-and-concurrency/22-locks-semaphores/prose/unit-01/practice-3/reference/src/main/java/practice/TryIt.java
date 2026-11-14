package practice;

import java.util.concurrent.CountDownLatch;
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
            LoadingDock dock = new LoadingDock(2);
            dock.arrive();
            dock.arrive();
            System.out.println("bays occupied: " + dock.occupied() + " of 2");
            CountDownLatch truckIn = onOwnThread("truck-PL-55", dock::arrive);
            System.out.println("third truck in while the dock is full: " + truckIn.await(300, TimeUnit.MILLISECONDS));
            dock.leave();
            System.out.println("third truck in after one left: " + truckIn.await(2, TimeUnit.SECONDS));
            CountDownLatch closed = onOwnThread("night-close", dock::awaitEmpty);
            dock.leave();
            System.out.println("gate locked with one truck still in: " + closed.await(300, TimeUnit.MILLISECONDS));
            dock.leave();
            System.out.println("gate locked after the last truck left: " + closed.await(2, TimeUnit.SECONDS));
            System.out.println("bays occupied at the end: " + dock.occupied());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    interface DockCall {
        void run() throws InterruptedException;
    }

    private static CountDownLatch onOwnThread(String name, DockCall call) {
        CountDownLatch returned = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                call.run();
                returned.countDown();
            } catch (InterruptedException | RuntimeException e) {
                System.out.println(name + " ended with: " + e);
            }
        }, name);
        t.setDaemon(true);
        t.start();
        return returned;
    }
}
