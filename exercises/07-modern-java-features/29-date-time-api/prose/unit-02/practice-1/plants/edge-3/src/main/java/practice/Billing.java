package practice;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Dates on invoices and statements for customers in many countries. The service runs in one
 * data centre; its clock is handed in, and that clock's zone is the data centre's.
 */
public class Billing {

    private final Clock clock;

    public Billing(Clock clock) {
        this.clock = clock;
    }

    /** The date printed on the invoice for a payment taken at {@code paidAt}. */
    LocalDate invoiceDate(Instant paidAt, ZoneId customerZone) {
        return LocalDate.ofInstant(paidAt, customerZone);
    }

    /** The first moment of {@code day} for the customer; a day's statement starts here. */
    Instant startOf(LocalDate day, ZoneId customerZone) {
        return day.atStartOfDay(customerZone).toInstant();
    }

    /** The customer's date at this moment, as the service's clock tells it. */
    LocalDate today(ZoneId customerZone) {
        return LocalDate.now(clock);
    }

    /** The payments that belong on the customer's statement for {@code day}, in the order given. */
    public List<Instant> statement(LocalDate day, ZoneId customerZone, List<Instant> payments) {
        Instant from = startOf(day, customerZone);
        Instant until = startOf(day.plusDays(1), customerZone);
        return payments.stream()
                .filter(p -> !p.isBefore(from) && p.isBefore(until))
                .toList();
    }

    /** The statement for the customer's current day. */
    public List<Instant> todaysStatement(ZoneId customerZone, List<Instant> payments) {
        return statement(today(customerZone), customerZone, payments);
    }
}
