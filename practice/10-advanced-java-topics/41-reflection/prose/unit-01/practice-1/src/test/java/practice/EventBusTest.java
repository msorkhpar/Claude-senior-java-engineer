package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class EventBusTest {

    record UserSignedUp(String email) {}
    record CartAbandoned(String cartId) {}
    record RefundIssued(String refundId) {}

    /** Every listener writes what it received here, as "method:event". */
    private static final List<String> received = new ArrayList<>();

    static class Mailer {
        @EventBus.Subscribe
        public void welcome(UserSignedUp e) { received.add("welcome:" + e.email()); }

        @EventBus.Subscribe
        public void remind(CartAbandoned e) { received.add("remind:" + e.cartId()); }

        public void unsubscribe(UserSignedUp e) { received.add("unsubscribe:" + e.email()); }
    }

    static class Recovery {
        @EventBus.Subscribe
        void retarget(CartAbandoned e) { received.add("retarget:" + e.cartId()); }
    }

    abstract static class AuditedListener {
        @EventBus.Subscribe
        public void audit(UserSignedUp e) { received.add("audit:" + e.email()); }
    }

    static class Onboarding extends AuditedListener {
        @EventBus.Subscribe
        public void start(UserSignedUp e) { received.add("start:" + e.email()); }
    }

    interface Listener<E> {
        void on(E event);
    }

    static class Refunds implements Listener<RefundIssued> {
        @EventBus.Subscribe
        @Override
        public void on(RefundIssued e) { received.add("on:" + e.refundId()); }
    }

    private EventBus busWith(Object listener) {
        received.clear();
        EventBus bus = new EventBus();
        bus.register(listener);
        return bus;
    }

    @Test
    void aSubscriberReceivesTheEventsItsParameterAccepts() {
        EventBus bus = busWith(new Mailer());

        bus.publish(new UserSignedUp("ada@example.com"));
        bus.publish(new CartAbandoned("c-310"));

        assertThat(received).containsExactlyInAnyOrder("welcome:ada@example.com", "remind:c-310");
    }

    @Test
    void aMethodWithoutTheAnnotationReceivesNothing() {
        EventBus bus = busWith(new Mailer());

        bus.publish(new UserSignedUp("lin@example.com"));

        assertThat(received).doesNotContain("unsubscribe:lin@example.com");
    }

    @Test
    void aSubscriberThatIsNotPublicIsCalled() {
        EventBus bus = busWith(new Recovery());

        bus.publish(new CartAbandoned("c-77"));

        assertThat(received).containsExactly("retarget:c-77");
    }

    @Test
    void aSubscriberInheritedFromAnAbstractListenerIsCalled() {
        EventBus bus = busWith(new Onboarding());

        bus.publish(new UserSignedUp("kim@example.com"));

        assertThat(received).containsExactlyInAnyOrder("start:kim@example.com", "audit:kim@example.com");
    }

    @Test
    void aSubscriberThatImplementsAGenericInterfaceIsCalledOnceAndOnlyForItsEvent() {
        EventBus bus = busWith(new Refunds());

        bus.publish(new RefundIssued("r-4521"));
        assertThatCode(() -> bus.publish(new CartAbandoned("c-9"))).doesNotThrowAnyException();

        assertThat(received).containsExactly("on:r-4521");
    }
}
