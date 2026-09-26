package practice;

public class LegacyApi {

    /**
     * @deprecated Use {@link #newMethod()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public String oldMethod() {
        return "old result";
    }

    /** The replacement for oldMethod(). */
    public String newMethod() {
        return "new result";
    }

    /**
     * @deprecated Use {@link #modernCalculation(int, int)}, which refuses overflow.
     */
    @Deprecated(since = "1.5")
    public int legacyCalculation(int a, int b) {
        return a + b;
    }

    /** The replacement sum: refuses overflow. */
    public int modernCalculation(int a, int b) {
        return Math.addExact(a, b);
    }
}
