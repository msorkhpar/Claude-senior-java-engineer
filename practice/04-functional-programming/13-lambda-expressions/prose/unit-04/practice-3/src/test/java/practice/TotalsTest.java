package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TotalsTest {

    private static final Order BIG = new Order(Status.COMPLETED, List.of(new Item(10.0), new Item(2.5)));
    private static final Order SMALL = new Order(Status.COMPLETED, List.of(new Item(4.0)));
    private static final Order PENDING = new Order(Status.PENDING, List.of(new Item(7.5)));

    @Test
    void addsUpOrdersAndCustomers() {
        Customer ann = new Customer("Ann", List.of(BIG, PENDING));
        Customer ben = new Customer("Ben", List.of(SMALL));

        assertThat(Totals.orderTotal(BIG)).isEqualTo(12.5, within(1e-9));
        Order refunded = new Order(Status.COMPLETED, List.of(new Item(10.0), new Item(-2.5)));
        assertThat(Totals.orderTotal(refunded)).isEqualTo(7.5, within(1e-9));
        assertThat(Totals.customerTotal(new Customer("Di", List.of(refunded)))).isEqualTo(7.5, within(1e-9));
        assertThat(Totals.customerTotal(ann)).isEqualTo(20.0, within(1e-9));
        assertThat(Totals.customerTotals(List.of(ann, ben))).containsExactly(20.0, 4.0);
        assertThat(Totals.completedRevenue(List.of(BIG, SMALL))).isEqualTo(16.5, within(1e-9));
    }

    @Test
    void emptyOrdersAndCustomersTotalZero() {
        Order empty = new Order(Status.COMPLETED, List.of());

        assertThat(Totals.orderTotal(empty)).isEqualTo(0.0);
        assertThat(Totals.customerTotal(new Customer("Cy", List.of()))).isEqualTo(0.0);
        assertThat(Totals.customerTotals(List.of(new Customer("Cy", List.of(empty))))).containsExactly(0.0);
    }

    @Test
    void pendingOrdersAreLeftOut() {
        assertThat(Totals.completedRevenue(List.of(BIG, PENDING))).isEqualTo(12.5, within(1e-9));
        Order unknown = new Order(null, List.of(new Item(3.0)));
        assertThat(Totals.completedRevenue(List.of(BIG, unknown))).isEqualTo(12.5, within(1e-9));
    }

    @Test
    void nonPositivePricesAreSkipped() {
        Order refunds = new Order(Status.COMPLETED, List.of(new Item(5.0), new Item(-3.0), new Item(0.0)));

        assertThat(Totals.completedRevenue(List.of(refunds))).isEqualTo(5.0, within(1e-9));
    }
}
