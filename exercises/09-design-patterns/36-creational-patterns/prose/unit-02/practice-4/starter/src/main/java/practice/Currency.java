package practice;

public final class Currency {

    private Currency(String code) {
    }

    /** Returns the one cached Currency for a three-capital-letter code. */
    public static Currency of(String code) {
        throw new UnsupportedOperationException("write of");
    }

    public String code() {
        throw new UnsupportedOperationException("write code");
    }
}
