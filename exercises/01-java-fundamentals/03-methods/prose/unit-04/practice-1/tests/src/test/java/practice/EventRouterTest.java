package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventRouterTest {

    @Test
    void parksEventsOfAKindItHasNoHandlerFor() {
        EventRouter router = new EventRouter();

        router.routeAll(List.of("heartbeat", 17));

        assertThat(router.journal()).containsExactly("parked heartbeat", "parked 17");
    }

    @Test
    void creditsAPaymentTakenOffTheQueue() {
        EventRouter router = new EventRouter();

        router.routeAll(List.of("heartbeat", new EventRouter.Payment("P-881", 4_250)));

        assertThat(router.journal()).containsExactly("parked heartbeat", "credit P-881 4250");
    }

    @Test
    void debitsARefundEvenThoughItIsAlsoAPayment() {
        EventRouter router = new EventRouter();

        router.routeAll(List.of(new EventRouter.Refund("R-302", 990), new EventRouter.Payment("P-882", 120)));

        assertThat(router.journal()).containsExactly("debit R-302 990", "credit P-882 120");
    }
}
