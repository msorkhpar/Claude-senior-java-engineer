package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reads the text of a thread dump ({@code jcmd <pid> Thread.print} or {@code jstack <pid>}) and
 * reports, for every thread blocked on a monitor, which threads hold that monitor.
 */
public class DumpReader {

    /** One thread of the dump: its name and the lines below its header. */
    record ThreadEntry(String name, List<String> lines) {}

    /** Each blocked thread's name, mapped to the names of the threads holding what it waits for. */
    public static Map<String, List<String>> whoBlocksWhom(String dump) {
        List<ThreadEntry> threads = threads(dump);
        Map<String, List<String>> holders = new TreeMap<>();       // monitor -> threads holding it
        for (ThreadEntry t : threads) {
            for (String monitor : held(t)) {
                holders.computeIfAbsent(monitor, k -> new ArrayList<>()).add(t.name());
            }
        }
        Map<String, List<String>> report = new TreeMap<>();
        for (ThreadEntry t : threads) {
            String monitor = waitingFor(t);
            if (monitor != null && holders.containsKey(monitor)) {
                report.put(t.name(), holders.get(monitor).stream().sorted().toList());
            }
        }
        return report;
    }

    /** Splits a dump into threads. A thread's block starts with its quoted name and ends at a blank line. */
    static List<ThreadEntry> threads(String dump) {
        List<ThreadEntry> threads = new ArrayList<>();
        String name = null;
        List<String> lines = new ArrayList<>();
        for (String line : dump.split("\n", -1)) {
            if (line.startsWith("\"")) {
                name = line.substring(1, line.indexOf('"', 1));
                lines = new ArrayList<>();
            } else if (line.isBlank()) {
                if (name != null) threads.add(new ThreadEntry(name, lines));
                name = null;
            } else if (name != null) {
                lines.add(line.strip());
            }
        }
        if (name != null) threads.add(new ThreadEntry(name, lines));
        return threads;
    }

    /** The monitor this thread is blocked trying to enter, or null when it isn't blocked on one. */
    static String waitingFor(ThreadEntry t) {
        // TODO: A thread can be stopped in many ways: parked on a latch, sleeping, waiting for a
        //       notify, or blocked at the entry of a synchronized block. Return the monitor only
        //       in the last case, in a form that held(...) returns for the thread holding it.
        throw new IllegalStateException("TODO: waitingFor");
    }

    /** The monitors this thread holds at the moment of the dump. */
    static Set<String> held(ThreadEntry t) {
        // TODO: Return every monitor this thread really holds while the dump is taken, so that
        //       whoBlocksWhom names exactly the threads another thread is waiting for.
        throw new IllegalStateException("TODO: held");
    }
}
