package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DispatcherTest {

    private static final Dispatcher.Courier NOOR = new Dispatcher.Courier("Noor", "north");
    private static final Dispatcher.Courier TAMSIN = new Dispatcher.Courier("Tamsin", "north");
    private static final Dispatcher.Courier OKKO = new Dispatcher.Courier("Okko", "north");
    private static final Dispatcher.Courier VIDA = new Dispatcher.Courier("Vida", "north");
    private static final Dispatcher.Courier IBRA = new Dispatcher.Courier("Ibra", "north");
    private static final Dispatcher.Courier MARGIT = new Dispatcher.Courier("Margit", "north");

    private static final List<Dispatcher.Courier> NEAREST_FIRST = List.of(NOOR, TAMSIN, OKKO, VIDA, IBRA, MARGIT);

    /** A courier service that knows who is free and records every request it gets. */
    private static final class Service implements Dispatcher.Availability {
        final Set<String> free;
        final List<String> asked = new ArrayList<>();

        Service(String... free) {
            this.free = Set.of(free);
        }

        @Override
        public boolean isFree(Dispatcher.Courier courier) {
            asked.add(courier.name());
            return free.contains(courier.name());
        }
    }

    @Test
    void theJobGoesToTheNearestFreeCourier() {
        Service service = new Service("Okko", "Ibra");

        assertThat(new Dispatcher(service).assign(NEAREST_FIRST)).isEqualTo("Okko");
    }

    @Test
    void theJobIsQueuedWhenNobodyIsFree() {
        Service service = new Service();

        assertThat(new Dispatcher(service).assign(NEAREST_FIRST)).isEqualTo("queued");
    }

    @Test
    void fastDeliveryIsPromisedOnlyWhenSomeoneIsFree() {
        assertThat(new Dispatcher(new Service("Margit")).canPromiseFastDelivery(NEAREST_FIRST)).isTrue();
        assertThat(new Dispatcher(new Service()).canPromiseFastDelivery(NEAREST_FIRST)).isFalse();
        assertThat(new Dispatcher(new Service("Noor")).canPromiseFastDelivery(List.of())).isFalse();
    }

    @Test
    void assigningAJobAsksNobodyBeyondTheCourierWhoGetsIt() {
        Service service = new Service("Okko", "Ibra");

        new Dispatcher(service).assign(NEAREST_FIRST);

        assertThat(service.asked).containsExactly("Noor", "Tamsin", "Okko");
    }

    @Test
    void thePromiseCheckStopsAskingOnceItHasItsAnswer() {
        Service service = new Service("Tamsin", "Vida");

        new Dispatcher(service).canPromiseFastDelivery(NEAREST_FIRST);

        assertThat(service.asked).containsExactly("Noor", "Tamsin");
    }
}
