package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopTest {

    @Test
    void aShopsDashboardCountsItsOrders() {
        Shop.Running shop = Shop.start();
        int before = shop.metrics().get("orders");
        shop.checkout().order("K-401");
        shop.checkout().order("K-402");
        assertThat(shop.metrics().get("orders")).isEqualTo(before + 2);
    }

    @Test
    void aServiceCountsIntoTheMetricsItIsGiven() {
        Shop.Metrics metrics = new Shop.Metrics();
        new Shop.Checkout(metrics).order("K-501");
        new Shop.Refunds(metrics).refund("K-501");
        assertThat(metrics.get("orders")).isEqualTo(1);
        assertThat(metrics.get("refunds")).isEqualTo(1);
    }

    @Test
    void refundsShowOnTheSameDashboardAsOrders() {
        Shop.Running shop = Shop.start();
        int before = shop.metrics().get("refunds");
        shop.checkout().order("K-601");
        shop.refunds().refund("K-601");
        assertThat(shop.metrics().get("refunds")).isEqualTo(before + 1);
    }

    @Test
    void twoShopsInOneJvmCountSeparately() {
        Shop.Running bakery = Shop.start();
        Shop.Running florist = Shop.start();
        for (int i = 0; i < 3; i++) bakery.checkout().order("B-" + i);
        florist.checkout().order("F-1");
        assertThat(bakery.metrics().get("orders")).isEqualTo(3);
        assertThat(florist.metrics().get("orders")).isEqualTo(1);
    }
}
