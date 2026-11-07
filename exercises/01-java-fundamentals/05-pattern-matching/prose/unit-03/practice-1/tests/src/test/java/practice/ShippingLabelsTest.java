package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static practice.ShippingLabels.Address;
import static practice.ShippingLabels.Customer;
import static practice.ShippingLabels.Order;

class ShippingLabelsTest {

    private static Address lyon() {
        return new Address("Lyon", "69002");
    }

    private static Address porto() {
        return new Address("Porto", "4050-001");
    }

    @Test
    void labelsEachOrderForItsCustomersAddress() {
        List<String> lines = ShippingLabels.labels(List.of(
                new Order("ORD-311", new Customer("Jane Doe", lyon(), lyon())),
                new Order("ORD-312", new Customer("John Roe", porto(), porto()))));

        assertThat(lines).containsExactly("ORD-311 Jane Doe, 69002 Lyon", "ORD-312 John Roe, 4050-001 Porto");
    }

    @Test
    void rejectsAnEventThatIsNotAnOrder() {
        assertThatThrownBy(() -> ShippingLabels.shipTo("ORD-313")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ShippingLabels.shipTo(new Customer("Jane Doe", lyon(), lyon())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ShippingLabels.shipTo(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shipsToTheShippingAddressWhenItDiffersFromTheBillingOne() {
        String line = ShippingLabels.shipTo(new Order("ORD-320", new Customer("Jane Doe", lyon(), porto())));

        assertThat(line).isEqualTo("ORD-320 Jane Doe, 4050-001 Porto");
    }

    @Test
    void shipsToTheBillingAddressWhenNoShippingAddressIsOnFile() {
        String line = ShippingLabels.shipTo(new Order("ORD-330", new Customer("John Roe", porto(), null)));

        assertThat(line).isEqualTo("ORD-330 John Roe, 4050-001 Porto");
    }

    @Test
    void shipsToTheShippingAddressWhenNoBillingAddressIsOnFile() {
        String line = ShippingLabels.shipTo(new Order("ORD-340", new Customer("Jane Doe", null, lyon())));

        assertThat(line).isEqualTo("ORD-340 Jane Doe, 69002 Lyon");
    }

    @Test
    void aCustomerWithNoAddressCollectsTheParcel() {
        String line = ShippingLabels.shipTo(new Order("ORD-350", new Customer("John Roe", null, null)));

        assertThat(line).isEqualTo("ORD-350 John Roe, PICKUP");
    }
}
