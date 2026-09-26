package practice;

public final class InlinePolicy {

    public enum Decision { INLINE, TOO_BIG, MEGAMORPHIC }

    private final int maxInlineSize;
    private final int freqInlineSize;

    public InlinePolicy(int maxInlineSize, int freqInlineSize) {
        this.maxInlineSize = maxInlineSize;
        this.freqInlineSize = freqInlineSize;
    }

    /** HotSpot's defaults: MaxInlineSize 35, FreqInlineSize 325. */
    public static InlinePolicy defaults() {
        return new InlinePolicy(35, 325);
    }

    /** Whether a call to a method of {@code bytecodeSize} bytes is inlined at this call site. */
    public Decision decide(int bytecodeSize, boolean hot, int receiverTypes) {
        if (receiverTypes >= 2) {
            return Decision.MEGAMORPHIC;
        }
        int limit = hot ? freqInlineSize : maxInlineSize;
        return bytecodeSize <= limit ? Decision.INLINE : Decision.TOO_BIG;
    }
}
