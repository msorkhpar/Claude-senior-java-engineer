package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JitFlags {

    private final Map<String, String> values;

    private JitFlags(Map<String, String> values) {
        this.values = values;
    }

    /** Reads the JIT flags from a java command line; other arguments are ignored. */
    public static JitFlags parse(List<String> args) {
        Map<String, String> values = new HashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("-XX:") || arg.length() < 5) {
                continue;
            }
            String body = arg.substring(4);
            if (body.startsWith("+")) {
                values.put(body.substring(1), "true");
            } else if (body.startsWith("-")) {
                values.put(body.substring(1), "false");
            } else if (body.contains("=")) {
                int eq = body.indexOf('=');
                values.put(body.substring(0, eq), body.substring(eq + 1));
            }
        }
        JitFlags flags = new JitFlags(values);
        int level = flags.number("TieredStopAtLevel", 4);
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("TieredStopAtLevel must be 0 to 4: " + level);
        }
        if (flags.bool("PrintInlining", false) && !flags.bool("UnlockDiagnosticVMOptions", false)) {
            throw new IllegalArgumentException("PrintInlining needs -XX:+UnlockDiagnosticVMOptions");
        }
        return flags;
    }

    private boolean bool(String name, boolean fallback) {
        String v = values.get(name);
        return v == null ? fallback : Boolean.parseBoolean(v);
    }

    private int number(String name, int fallback) {
        String v = values.get(name);
        return v == null ? fallback : Integer.parseInt(v);
    }

    public boolean tiered() {
        return bool("TieredCompilation", true);
    }

    /** The highest compilation tier: the stop level when tiered, 4 when not. */
    public int topTier() {
        return tiered() ? number("TieredStopAtLevel", 4) : 4;
    }

    public int maxInlineSize() {
        return number("MaxInlineSize", 35);
    }

    public int freqInlineSize() {
        return number("FreqInlineSize", 325);
    }

    public boolean printCompilation() {
        return bool("PrintCompilation", false);
    }

    public boolean printInlining() {
        return bool("PrintInlining", false);
    }
}
