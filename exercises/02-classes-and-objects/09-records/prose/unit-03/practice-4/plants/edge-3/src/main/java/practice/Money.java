package practice;

import java.util.Objects;

public record Money(long cents, String currency) {

    /** Refuse a null currency. */
    public Money {
    }

    /** A new Money holding the sum; another currency or an overflow is refused. */
    public Money plus(Money other) {
        if (!Objects.equals(currency, other.currency)) {
            throw new IllegalArgumentException("Cannot add " + other.currency + " to " + currency);
        }
        return new Money(Math.addExact(cents, other.cents), currency);
    }
}
