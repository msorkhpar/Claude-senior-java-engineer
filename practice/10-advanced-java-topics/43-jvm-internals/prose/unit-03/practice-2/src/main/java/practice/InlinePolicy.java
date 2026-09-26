package practice;

public final class InlinePolicy {

    public enum Decision { INLINE, TOO_BIG, MEGAMORPHIC }

    public InlinePolicy(int maxInlineSize, int freqInlineSize) {
    }

    /** HotSpot's defaults: MaxInlineSize 35, FreqInlineSize 325. */
    public static InlinePolicy defaults() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Whether a call to a method of {@code bytecodeSize} bytes is inlined at this call site. */
    public Decision decide(int bytecodeSize, boolean hot, int receiverTypes) {
        throw new UnsupportedOperationException("TODO");
    }
}
