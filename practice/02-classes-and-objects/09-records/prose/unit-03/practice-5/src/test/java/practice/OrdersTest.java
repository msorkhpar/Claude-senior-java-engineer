package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrdersTest {

    @Test
    void totalsEachCustomer() {
        List<Orders.Order> orders = List.of(
                new Orders.Order("ann", 500), new Orders.Order("bob", 300), new Orders.Order("ann", 250));
        assertThat(Orders.totals(orders)).containsExactly(
                new Orders.CustomerTotal("ann", 750, 2),
                new Orders.CustomerTotal("bob", 300, 1));
    }

    @Test
    void equalTotalsAreOrderedByName() {
        List<Orders.Order> orders = List.of(
                new Orders.Order("zed", 100), new Orders.Order("cid", 100), new Orders.Order("ann", 100),
                new Orders.Order("max", 100), new Orders.Order("bea", 100), new Orders.Order("dan", 900));
        assertThat(Orders.totals(orders)).extracting(Orders.CustomerTotal::customer)
                .containsExactly("dan", "ann", "bea", "cid", "max", "zed");
    }

    @Test
    void totalsMayExceedAnInt() {
        List<Orders.Order> orders = List.of(
                new Orders.Order("ann", 2_000_000_000L), new Orders.Order("ann", 2_000_000_000L));
        assertThat(Orders.totals(orders)).containsExactly(new Orders.CustomerTotal("ann", 4_000_000_000L, 2));
    }

    @Test
    void noOrdersGiveNoTotals() {
        assertThat(Orders.totals(List.of())).isEmpty();
    }

    @Test
    void namesBuiltSeparatelyAreOneCustomer() {
        List<Orders.Order> orders = List.of(
                new Orders.Order(new String("ann"), 500),
                new Orders.Order(new StringBuilder("an").append('n').toString(), 250));
        assertThat(Orders.totals(orders)).containsExactly(new Orders.CustomerTotal("ann", 750, 2));
    }
}
