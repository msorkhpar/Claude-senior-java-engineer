package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillingContactsTest {

    private BillingContacts contacts() {
        BillingContacts contacts = new BillingContacts();
        contacts.add(new BillingContacts.Account("ACME-0042", "ap@acme.example.com"));
        contacts.add(new BillingContacts.Account("GLOBEX-7", null));
        return contacts;
    }

    @Test
    void anInvoiceGoesToTheAccountsBillingAddress() {
        assertThat(contacts().invoiceAddress("ACME-0042")).isEqualTo("ap@acme.example.com");
    }

    @Test
    void anAccountWithoutABillingAddressGoesToReceivables() {
        assertThat(contacts().invoiceAddress("GLOBEX-7")).isEqualTo(BillingContacts.RECEIVABLES);
    }

    @Test
    void anUnknownAccountFailsWithItsIdInTheMessage() {
        assertThatThrownBy(() -> contacts().invoiceAddress("INITECH-3"))
                .isInstanceOf(BillingContacts.UnknownAccountException.class)
                .hasMessageContaining("INITECH-3");
    }
}
