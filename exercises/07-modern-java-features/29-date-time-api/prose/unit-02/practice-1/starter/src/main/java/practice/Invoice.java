package practice;

import java.time.LocalDate;

public final class Invoice {

    public Invoice(LocalDate issued, int termDays) {
    }

    public LocalDate issued() {
        throw new UnsupportedOperationException("write issued");
    }

    public int termDays() {
        throw new UnsupportedOperationException("write termDays");
    }

    /** The issue date plus the term, moved to the next Monday if it falls on a weekend. */
    public LocalDate dueDate() {
        throw new UnsupportedOperationException("write dueDate");
    }

    /** An invoice with the same issue date and a term {@code days} longer. */
    public Invoice extendedBy(int days) {
        throw new UnsupportedOperationException("write extendedBy");
    }
}
