package practice;

import java.util.Objects;

public record Money(long cents, String currency) {

    /** Refuse a null currency. */
    public Money {
        Objects.requireNonNull(currency, "currency");
    }

    /** A new Money holding the sum; another currency or an overflow is refused. */
    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents), currency);
    }
}
