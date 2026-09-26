package practice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class Invoice {

    private final LocalDate issued;
    private final int termDays;

    public Invoice(LocalDate issued, int termDays) {
        this.issued = issued; // a null slips in and fails only later
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
        return new Invoice(issued, termDays + days);
    }
}
