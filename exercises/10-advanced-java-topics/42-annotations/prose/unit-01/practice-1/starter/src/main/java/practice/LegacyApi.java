package practice;

public class LegacyApi {

    /** The old way: deprecated since 2.0 and for removal. */
    public String oldMethod() {
        throw new UnsupportedOperationException("TODO");
    }

    /** The replacement for oldMethod(). */
    public String newMethod() {
        throw new UnsupportedOperationException("TODO");
    }

    /** The old sum, wrapping on overflow: deprecated since 1.5, not for removal. */
    public int legacyCalculation(int a, int b) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The replacement sum: refuses overflow. */
    public int modernCalculation(int a, int b) {
        throw new UnsupportedOperationException("TODO");
    }
}
