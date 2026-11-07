package practice;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTemplateTest {

    @Test
    void fillsEachNamedValue() {
        MessageTemplate t = new MessageTemplate("Hello {customer}, ticket {ticket} is now {status}.");

        String text = t.render(Map.of("customer", "Ravi", "ticket", "T-5521", "status", "closed"));

        assertThat(text).isEqualTo("Hello Ravi, ticket T-5521 is now closed.");
    }

    @Test
    void leavesANameWithoutAValueAsItIs() {
        MessageTemplate t = new MessageTemplate("Hi {customer}, your agent is {agent}.");

        assertThat(t.render(Map.of("customer", "Ravi"))).isEqualTo("Hi Ravi, your agent is {agent}.");
    }

    @Test
    void showsAValueWithDollarSignsAndBackslashesExactly() {
        MessageTemplate t = new MessageTemplate("Refund of {refund} sent; receipt saved as {file}.");

        String text = t.render(Map.of("refund", "$18.40", "file", "D:\\receipts\\r7.pdf"));

        assertThat(text).isEqualTo("Refund of $18.40 sent; receipt saved as D:\\receipts\\r7.pdf.");
    }

    @Test
    void aDottedNameMatchesOnlyItself() {
        MessageTemplate t = new MessageTemplate("Invoice {invoice.no} replaces {invoice-no}.");

        assertThat(t.render(Map.of("invoice.no", "INV-88"))).isEqualTo("Invoice INV-88 replaces {invoice-no}.");
    }

    @Test
    void aNameUsedTwiceIsFilledBothTimes() {
        MessageTemplate t = new MessageTemplate("{customer}, thanks. We'll reply to {customer} within a day.");

        assertThat(t.render(Map.of("customer", "Ana"))).isEqualTo("Ana, thanks. We'll reply to Ana within a day.");
    }
}
