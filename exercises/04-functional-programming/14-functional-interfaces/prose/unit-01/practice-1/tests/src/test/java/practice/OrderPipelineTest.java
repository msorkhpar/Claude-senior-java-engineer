package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class OrderPipelineTest {

    private final Map<String, Integer> stock = new HashMap<>(Map.of("LAMP-3", 2, "DESK-9", 0));
    private final List<String> reserved = new ArrayList<>();
    private final List<String> emailed = new ArrayList<>();
    private final List<String> audited = new ArrayList<>();

    private final Consumer<OrderPipeline.Order> reserve = order -> {
        int left = stock.get(order.sku());
        if (left < order.quantity()) {
            throw new IllegalStateException("only " + left + " of " + order.sku() + " left for " + order.id());
        }
        stock.put(order.sku(), left - order.quantity());
        reserved.add(order.id());
    };
    private final Consumer<OrderPipeline.Order> confirm = order -> emailed.add(order.id());
    private final Consumer<OrderPipeline.Order> audit = order -> audited.add(order.id());

    @Test
    void placesAnOrderThatIsInStock() {
        OrderPipeline.build(reserve, confirm, audit).accept(new OrderPipeline.Order("K-7", "LAMP-3", 2));

        assertThat(reserved).containsExactly("K-7");
        assertThat(emailed).containsExactly("K-7");
        assertThat(audited).containsExactly("K-7");
        assertThat(stock).containsEntry("LAMP-3", 0);
    }

    @Test
    void anOrderThatCannotBeReservedIsNotConfirmed() {
        Consumer<OrderPipeline.Order> checkout = OrderPipeline.build(reserve, confirm, audit);

        catchThrowable(() -> checkout.accept(new OrderPipeline.Order("K-8", "DESK-9", 1)));

        assertThat(emailed).isEmpty();
    }

    @Test
    void theCheckoutLearnsThatAnOrderCannotBeReserved() {
        IllegalStateException noStock = new IllegalStateException("warehouse closed");
        Consumer<OrderPipeline.Order> checkout = OrderPipeline.build(
                order -> { throw noStock; }, confirm, audit);

        assertThatThrownBy(() -> checkout.accept(new OrderPipeline.Order("K-9", "LAMP-3", 1)))
                .isSameAs(noStock);
    }

    @Test
    void aDeploymentWithoutAComplianceLogStillTakesOrders() {
        Consumer<OrderPipeline.Order> checkout = OrderPipeline.build(reserve, confirm, null);
        checkout.accept(new OrderPipeline.Order("K-10", "LAMP-3", 1));
        checkout.accept(new OrderPipeline.Order("K-11", "LAMP-3", 1));

        assertThat(reserved).containsExactly("K-10", "K-11");
        assertThat(emailed).containsExactly("K-10", "K-11");
    }

    @Test
    void anOrderWhoseEmailFailedIsNotInTheComplianceLog() {
        Consumer<OrderPipeline.Order> mailDown = order -> {
            throw new UncheckedIOException(new IOException("smtp.example.com refused the connection"));
        };
        Consumer<OrderPipeline.Order> checkout = OrderPipeline.build(reserve, mailDown, audit);

        catchThrowable(() -> checkout.accept(new OrderPipeline.Order("K-12", "LAMP-3", 1)));

        assertThat(audited).isEmpty();
    }
}
