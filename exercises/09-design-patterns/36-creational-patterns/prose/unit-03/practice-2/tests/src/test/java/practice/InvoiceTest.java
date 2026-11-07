package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {

    @Test
    void anInvoiceHasItsCustomerAndItsLinesInOrder() {
        Invoice inv = Invoice.builder().customer("Northwind Ltd")
                .line("Team plan, 12 seats: 348.00").line("Extra storage 50 GB: 15.00")
                .build();

        assertThat(inv.customer()).isEqualTo("Northwind Ltd");
        assertThat(inv.lines()).containsExactly("Team plan, 12 seats: 348.00", "Extra storage 50 GB: 15.00");
    }

    @Test
    void anInvoiceBuiltFromACopyHasThePlansLinesAndItsOwn() {
        Invoice.Builder team = Invoice.builder().line("Team plan base fee: 99.00");

        Invoice inv = team.copy().customer("Contoso GmbH").line("Overage 1,200 calls: 6.00").build();

        assertThat(inv.customer()).isEqualTo("Contoso GmbH");
        assertThat(inv.lines()).containsExactly("Team plan base fee: 99.00", "Overage 1,200 calls: 6.00");
    }

    @Test
    void anIssuedInvoiceDoesNotChangeWhenItsBuilderIsUsedAgain() {
        Invoice.Builder b = Invoice.builder().customer("Fabrikam").line("Starter plan: 19.00");
        Invoice march = b.build();

        b.line("Starter plan, late fee: 5.00");

        assertThat(march.lines()).containsExactly("Starter plan: 19.00");
    }

    @Test
    void codeThatReadsAnInvoiceCannotChangeIt() {
        Invoice inv = Invoice.builder().customer("Tailspin").line("Pro plan: 249.00").build();

        List<String> seen = inv.lines();
        try {
            seen.add("Credit: -249.00");
        } catch (UnsupportedOperationException refused) {
            // refusing the write is one way to keep the invoice as issued
        }

        assertThat(inv.lines()).containsExactly("Pro plan: 249.00");
    }

    @Test
    void aCustomersLinesStayOutOfThePlanAndTheNextCustomer() {
        Invoice.Builder pro = Invoice.builder().line("Pro plan base fee: 249.00");

        Invoice first = pro.copy().customer("Adatum").line("Overage 900 calls: 4.50").build();
        Invoice second = pro.copy().customer("Litware").build();

        assertThat(first.lines()).containsExactly("Pro plan base fee: 249.00", "Overage 900 calls: 4.50");
        assertThat(second.lines()).containsExactly("Pro plan base fee: 249.00");
    }
}
