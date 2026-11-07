package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventBusTest {

    interface Auditable {
    }

    static class InvoiceEvent {
    }

    static class InvoicePaid extends InvoiceEvent {
    }

    static class InvoiceVoided extends InvoiceEvent implements Auditable {
    }

    static class PasswordChanged implements Auditable {
    }

    static class CacheWarmed {
    }

    private final List<Object> received = new ArrayList<>();

    @Test
    void aHandlerReceivesEventsOfTheClassItSubscribedTo() {
        EventBus bus = new EventBus();
        bus.subscribe(PasswordChanged.class, received::add);
        PasswordChanged event = new PasswordChanged();

        assertThat(bus.publish(event)).isEqualTo(1);
        assertThat(received).containsExactly(event);
    }

    @Test
    void aHandlerIgnoresEventsOfAnUnrelatedClass() {
        EventBus bus = new EventBus();
        bus.subscribe(PasswordChanged.class, received::add);

        assertThat(bus.publish(new CacheWarmed())).isZero();
        assertThat(received).isEmpty();
    }

    @Test
    void theStartupCheckReportsOnlyClassesWithNoHandler() {
        EventBus bus = new EventBus();
        bus.subscribe(PasswordChanged.class, received::add);

        assertThat(bus.unhandled(List.of(PasswordChanged.class, CacheWarmed.class)))
                .containsExactly(CacheWarmed.class);
    }

    @Test
    void aHandlerForAParentClassReceivesEventsOfItsSubclasses() {
        EventBus bus = new EventBus();
        bus.subscribe(InvoiceEvent.class, received::add);
        InvoicePaid paid = new InvoicePaid();

        assertThat(bus.publish(paid)).isEqualTo(1);
        assertThat(received).containsExactly(paid);
    }

    @Test
    void anEventTheDecoderCouldNotMapReachesNoHandler() {
        EventBus bus = new EventBus();
        bus.subscribe(Object.class, received::add);
        bus.subscribe(Auditable.class, received::add);

        assertThat(bus.publish(null)).isZero();
        assertThat(received).isEmpty();
    }

    @Test
    void theStartupCheckCountsAHandlerForAnInterfaceTheClassImplements() {
        EventBus bus = new EventBus();
        bus.subscribe(Auditable.class, received::add);

        assertThat(bus.unhandled(List.of(PasswordChanged.class, InvoiceVoided.class, CacheWarmed.class)))
                .containsExactly(CacheWarmed.class);
    }

    @Test
    void theStartupCheckCountsAHandlerForAParentClass() {
        EventBus bus = new EventBus();
        bus.subscribe(Object.class, received::add);

        assertThat(bus.unhandled(List.of(InvoicePaid.class, CacheWarmed.class))).isEmpty();
    }
}
