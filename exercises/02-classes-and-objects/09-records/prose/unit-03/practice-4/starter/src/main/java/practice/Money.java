package practice;

import java.util.Objects;

public record Money(long cents, String currency) {

    /** Refuse a null currency. */
    public Money {
        throw new UnsupportedOperationException("write the compact constructor");
    }

    /** A new Money holding the sum; another currency or an overflow is refused. */
    public Money plus(Money other) {
        throw new UnsupportedOperationException("write plus");
    }
}
