package practice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public final class Invoice {

    private final LocalDate issued;
    private int termDays;

    public Invoice(LocalDate issued, int termDays) {
        this.issued = Objects.requireNonNull(issued, "issued");
        this.termDays = termDays;
    }

    public LocalDate issued() {
        return issued;
    }

    public int termDays() {
        return termDays;
    }

    public LocalDate dueDate() {
        LocalDate due = issued.plusDays(termDays);
        DayOfWeek day = due.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            due = due.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return due;
    }

    public Invoice extendedBy(int days) {
        termDays += days; // changes this invoice, the way a Date setter would
        return this;
    }
}
