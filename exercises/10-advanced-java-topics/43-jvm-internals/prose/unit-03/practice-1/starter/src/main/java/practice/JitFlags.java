package practice;

import java.util.List;

public final class JitFlags {

    private JitFlags() {
    }

    /** Reads the JIT flags from a java command line; other arguments are ignored. */
    public static JitFlags parse(List<String> args) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean tiered() {
        throw new UnsupportedOperationException("TODO");
    }

    /** The highest compilation tier: the stop level when tiered, 4 when not. */
    public int topTier() {
        throw new UnsupportedOperationException("TODO");
    }

    public int maxInlineSize() {
        throw new UnsupportedOperationException("TODO");
    }

    public int freqInlineSize() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean printCompilation() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean printInlining() {
        throw new UnsupportedOperationException("TODO");
    }
}
