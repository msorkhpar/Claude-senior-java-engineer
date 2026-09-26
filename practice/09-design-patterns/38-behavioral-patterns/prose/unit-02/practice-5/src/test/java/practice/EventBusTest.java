package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import practice.EventBus.Subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventBusTest {

    @Test
    void eachTypeReachesOnlyItsOwnListeners() {
        EventBus bus = new EventBus();
        List<Object> users = new ArrayList<>();
        List<Object> welcomes = new ArrayList<>();
        List<Object> orders = new ArrayList<>();
        bus.subscribe("user.created", users::add);
        Consumer<Object> welcome = welcomes::add;
        bus.subscribe("user.created", welcome);
        bus.subscribe("order.placed", orders::add);

        bus.publish("user.created", "ada");
        bus.publish("order.placed", 4711);
        bus.unsubscribe("user.created", welcome);
        bus.publish("user.created", "grace");

        assertThat(users).containsExactly("ada", "grace");
        assertThat(welcomes).containsExactly("ada");
        assertThat(orders).containsExactly(4711);
        assertThat(bus.listenerCount("user.created")).isEqualTo(1);
        assertThat(bus.eventTypes()).containsExactlyInAnyOrder("user.created", "order.placed");
        assertThatThrownBy(() -> bus.subscribe("user.created", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelRemovesOnlyItsOwnSubscription() {
        EventBus bus = new EventBus();
        List<Object> got = new ArrayList<>();
        Consumer<Object> listener = got::add;
        Subscription toA = bus.subscribe("a", listener);
        bus.subscribe("b", listener);

        toA.cancel();
        bus.publish("a", "from a");
        bus.publish("b", "from b");

        assertThat(got).containsExactly("from b");
        assertThat(bus.listenerCount("a")).isZero();
        assertThat(bus.listenerCount("b")).isEqualTo(1);
    }

    @Test
    void aBlankEventTypeIsRefused() {
        EventBus bus = new EventBus();

        assertThatThrownBy(() -> bus.subscribe("   ", data -> { })).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bus.subscribe("", data -> { })).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bus.publish(" \t", "x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bus.publish(null, "x")).isInstanceOf(IllegalArgumentException.class);
        assertThat(bus.eventTypes()).isEmpty();
    }

    @Test
    void eventTypesCannotChangeTheBus() {
        EventBus bus = new EventBus();
        List<Object> got = new ArrayList<>();
        bus.subscribe("user.created", got::add);

        Set<String> types = bus.eventTypes();
        try {
            types.remove("user.created");
        } catch (UnsupportedOperationException refused) {
            // an unmodifiable answer is fine too
        }
        bus.publish("user.created", "ada");

        assertThat(got).containsExactly("ada");
        assertThat(bus.eventTypes()).containsExactly("user.created");
    }

    @Test
    void unknownTypesAreHarmless() {
        EventBus bus = new EventBus();
        List<Object> got = new ArrayList<>();
        Consumer<Object> listener = got::add;
        bus.subscribe("known", listener);

        bus.publish("nobody.listens", 1);
        bus.unsubscribe("nobody.listens", listener);

        assertThat(bus.listenerCount("nobody.listens")).isZero();
        assertThat(got).isEmpty();
        assertThat(bus.listenerCount("known")).isEqualTo(1);
    }
}
