package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CheckoutTest {

    private static Checkout checkout() {
        return new Checkout(Map.of("SKU-TEA-250", 1_299L, "SKU-MUG-02", 850L, "SKU-KETTLE", 4_500L));
    }

    @Test
    void aFirstTimeCustomerGetsOneNewRow() {
        Checkout c = checkout();
        String id = c.customerId("jane.doe@example.com");
        assertThat(c.inserts).isEqualTo(1);
        assertThat(c.customers).containsEntry("jane.doe@example.com", id);
    }

    @Test
    void aFirstPriceComesFromTheCatalogue() {
        Checkout c = checkout();
        assertThat(c.price("SKU-MUG-02")).isEqualTo(850L);
        assertThat(c.price("SKU-KETTLE")).isEqualTo(4_500L);
        assertThat(c.catalogueCalls).isEqualTo(2);
        assertThat(c.priceCache).containsEntry("SKU-MUG-02", 850L);
    }

    @Test
    void aReturningCustomerKeepsTheirRowAndNoNewOneIsInserted() {
        Checkout c = checkout();
        String first = c.customerId("ops@example.com");
        String again = c.customerId("ops@example.com");
        String third = c.customerId("ops@example.com");
        assertThat(again).isEqualTo(first);
        assertThat(third).isEqualTo(first);
        assertThat(c.inserts).isEqualTo(1);
        assertThat(c.customers).containsEntry("ops@example.com", first);
    }

    @Test
    void aCachedPriceDoesNotCallTheCatalogueAgain() {
        Checkout c = checkout();
        c.price("SKU-TEA-250");
        long again = c.price("SKU-TEA-250");
        c.price("SKU-TEA-250");
        assertThat(again).isEqualTo(1_299L);
        assertThat(c.catalogueCalls).isEqualTo(1);
    }
}
