package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    private static final LocalDate MARCH_1 = LocalDate.parse("2024-03-01");

    @Test
    void computesAndExtendsTheDueDate() {
        Invoice invoice = new Invoice(MARCH_1, 14);
        assertThat(invoice.issued()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(invoice.termDays()).isEqualTo(14);
        assertThat(invoice.dueDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        Invoice extended = invoice.extendedBy(4);
        assertThat(extended.issued()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(extended.termDays()).isEqualTo(18);
        assertThat(extended.dueDate()).isEqualTo(LocalDate.of(2024, 3, 19));
    }

    @Test
    void aWeekendDueDateMovesToMonday() {
        assertThat(new Invoice(MARCH_1, 15).dueDate()).as("Saturday 16th").isEqualTo(LocalDate.of(2024, 3, 18));
        assertThat(new Invoice(MARCH_1, 16).dueDate()).as("Sunday 17th").isEqualTo(LocalDate.of(2024, 3, 18));
    }

    @Test
    void extendingLeavesTheOriginalAlone() {
        Invoice original = new Invoice(MARCH_1, 14);
        Invoice extended = original.extendedBy(4);
        assertThat(extended.termDays()).isEqualTo(18);
        assertThat(original.termDays()).as("the original invoice changed").isEqualTo(14);
        assertThat(original.dueDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @Test
    void aNullIssueDateIsRefused() {
        assertThatThrownBy(() -> new Invoice(null, 14)).isInstanceOf(NullPointerException.class);
    }
}
