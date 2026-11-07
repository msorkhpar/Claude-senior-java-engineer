package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<Integer, Integer> live = new LinkedHashMap<>();
        for (Line l : lines) {
            if (l.osr() || !l.method().equals(method)) continue;
            if (l.madeNotEntrant()) live.remove(l.id());
            else live.put(l.id(), l.tier());
        }
        int tier = 0;
        for (int t : live.values()) tier = t;
        return tier;
    }

    /**
     * How many times the JIT threw away {@code method}'s compiled code (for calls, not loops)
     * because the code could no longer be used, not counting the routine moves from one tier's
     * code to a newer compile.
     */
    int deoptimizations(String method) {
        Map<Integer, Integer> live = new HashMap<>();
        int count = 0;
        for (Line l : lines) {
            if (l.osr() || !l.method().equals(method)) continue;
            if (!l.madeNotEntrant()) {
                live.put(l.id(), l.tier());
                continue;
            }
            live.remove(l.id());
            boolean replacedByNewer = live.keySet().stream().anyMatch(other -> other > l.id());
            count++;
        }
        return count;
    }
}
