package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void buildsAnOrderWithEveryOption() {
        Order order = Order.builder("Ann").quantity(3).note("ring twice").express(true).build();
        assertThat(order.getCustomer()).isEqualTo("Ann");
        assertThat(order.getQuantity()).isEqualTo(3);
        assertThat(order.getNote()).isEqualTo("ring twice");
        assertThat(order.isExpress()).isTrue();
    }

    @Test
    void unsetOptionsTakeTheirDefaults() {
        Order order = Order.builder("Ann").build();
        assertThat(order.getCustomer()).isEqualTo("Ann");
        assertThat(order.getQuantity()).isEqualTo(1);
        assertThat(order.getNote()).isEmpty();
        assertThat(order.isExpress()).isFalse();
    }

    @Test
    void aQuantityBelowOneIsRefusedAtBuild() {
        Order.Builder builder = Order.builder("Ann").quantity(0);
        assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);
        Order.Builder negative = Order.builder("Ann").quantity(-2);
        assertThatThrownBy(negative::build).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theCustomerIsRequired() {
        assertThatThrownBy(() -> Order.builder(null)).isInstanceOf(NullPointerException.class);
    }
}
