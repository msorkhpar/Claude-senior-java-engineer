package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the output of a JVM started with -XX:+PrintCompilation and answers questions about one
 * method's compiled code. Lines that are not compile lines are skipped.
 */
public class CompileLog {

    /** One compile line: a compile installed, or (madeNotEntrant) the same compile id retired. */
    public record Line(long millis, int id, boolean osr, int tier, String method, boolean madeNotEntrant) {}

    private final List<Line> lines = new ArrayList<>();

    public CompileLog(List<String> log) {
        for (String text : log) {
            Line line = parse(text);
            if (line != null) lines.add(line);
        }
    }

    /** The parsed compile lines, in log order. */
    public List<Line> lines() {
        return List.copyOf(lines);
    }

    /** Parses one line such as "  17   11 %     4       Warmup::round @ 4 (26 bytes)", or returns null. */
    static Line parse(String text) {
        String t = text.trim();
        boolean notEntrant = t.endsWith("made not entrant");
        if (notEntrant) t = t.substring(0, t.length() - "made not entrant".length()).trim();
        String[] parts = t.split("\\s+");
        if (parts.length < 4 || !parts[0].matches("\\d+") || !parts[1].matches("\\d+")) return null;
        boolean osr = false;
        int i = 2;
        while (i < parts.length && !parts[i].matches("[0-4]")) {
            if (parts[i].contains("%")) osr = true;
            if (!parts[i].matches("[%sbn!]+")) return null;
            i++;
        }
        if (i + 1 >= parts.length) return null;
        return new Line(Long.parseLong(parts[0]), Integer.parseInt(parts[1]), osr,
                Integer.parseInt(parts[i]), parts[i + 1], notEntrant);
    }

    /**
     * The tier of the compiled code that a new call to {@code method} enters now, or 0 if a new
     * call runs in the interpreter. Loop (OSR) compiles don't take calls.
     */
    int currentTier(String method) {
        // TODO: Answer which code a call to this method made right now would run in, judging
        //       only from the log lines. 0 means the interpreter.
        throw new IllegalStateException("TODO: currentTier");
    }

    /**
     * How many times the JIT threw away {@code method}'s compiled code (for calls, not loops)
     * because the code could no longer be used, not counting the routine moves from one tier's
     * code to a newer compile.
     */
    int deoptimizations(String method) {
        // TODO: Count the times this method's code for calls was thrown away because it could no
        //       longer be used. The routine retirements as the method climbs the tiers don't count.
        throw new IllegalStateException("TODO: deoptimizations");
    }
}
