package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OrderValidatorTest {

    private static final Order FULL = new Order(List.of("book"), 12.5, "c-1");
    private static final Order EMPTY = new Order(List.of(), 0.0, null);
    private static final OrderValidator POSITIVE_TOTAL = order -> order.total() > 0;

    @Test
    void combinesChecksWithAndOrNegate() {
        OrderValidator full = OrderValidator.hasItems().and(POSITIVE_TOTAL).and(OrderValidator.hasCustomer());
        assertThat(full.validate(FULL)).isTrue();
        assertThat(full.validate(EMPTY)).isFalse();
        assertThat(full.validate(new Order(List.of("pen"), 3.0, null))).isFalse();

        OrderValidator either = OrderValidator.hasItems().or(OrderValidator.hasCustomer());
        assertThat(either.validate(new Order(List.of(), 0.0, "c-2"))).isTrue();
        assertThat(either.validate(EMPTY)).isFalse();

        assertThat(OrderValidator.hasItems().negate().validate(EMPTY)).isTrue();
        assertThat(OrderValidator.hasItems().negate().validate(FULL)).isFalse();

        assertThat(OrderValidator.hasCustomer().validate(new Order(List.of(), 0.0, "   "))).isTrue();
        assertThat(OrderValidator.hasItems().validate(new Order(List.of("pen"), 0.0, null))).isTrue();
        assertThat(OrderValidator.hasItems().validate(new Order(Arrays.asList((String) null), 0.0, null))).isTrue();
    }

    @Test
    void andSkipsTheSecondCheckOnceTheFirstFails() {
        AtomicInteger calls = new AtomicInteger();
        OrderValidator counted = order -> {
            calls.incrementAndGet();
            return true;
        };

        assertThat(OrderValidator.hasItems().and(counted).validate(EMPTY)).isFalse();
        assertThat(calls).hasValue(0);
    }

    @Test
    void orSkipsTheSecondCheckOnceTheFirstPasses() {
        AtomicInteger calls = new AtomicInteger();
        OrderValidator counted = order -> {
            calls.incrementAndGet();
            return false;
        };

        assertThat(OrderValidator.hasItems().or(counted).validate(FULL)).isTrue();
        assertThat(calls).hasValue(0);
    }
}
