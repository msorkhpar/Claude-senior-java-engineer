package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventBusTest {

    @Test
    void deliversEachEventToEveryListener() {
        EventBus bus = new EventBus();
        List<String> seen = new ArrayList<>();
        Consumer<String> a = e -> seen.add("A:" + e);
        Consumer<String> b = e -> seen.add("B:" + e);
        bus.subscribe(a);
        bus.subscribe(b);
        bus.fire("e1");
        assertThat(seen).containsExactly("A:e1", "B:e1");
        assertThat(bus.unsubscribe(a)).isTrue();
        assertThat(bus.unsubscribe(a)).isFalse();
        bus.fire("e2");
        assertThat(seen).containsExactly("A:e1", "B:e1", "B:e2");
        assertThat(bus.listeners()).containsExactly(b);
    }

    @Test
    void aListenerMayUnsubscribeDuringAnEvent() {
        EventBus bus = new EventBus();
        List<String> seen = new ArrayList<>();
        Consumer<String>[] once = new Consumer[1];
        once[0] = e -> {
            seen.add("A:" + e);
            bus.unsubscribe(once[0]);
        };
        bus.subscribe(once[0]);
        bus.subscribe(e -> seen.add("B:" + e));
        bus.subscribe(e -> seen.add("C:" + e));
        bus.fire("e1");
        bus.fire("e2");
        assertThat(seen).containsExactly("A:e1", "B:e1", "C:e1", "B:e2", "C:e2");
    }

    @Test
    void aListenerAddedDuringAnEventWaitsForTheNext() {
        EventBus bus = new EventBus();
        List<String> seen = new ArrayList<>();
        Consumer<String> c = e -> seen.add("C:" + e);
        bus.subscribe(e -> {
            seen.add("A:" + e);
            if (e.equals("e1")) {
                bus.subscribe(c);
            }
        });
        bus.fire("e1");
        bus.fire("e2");
        assertThat(seen).containsExactly("A:e1", "A:e2", "C:e2");
    }

    @Test
    void listenersReturnsACopyThatDoesNotChange() {
        EventBus bus = new EventBus();
        Consumer<String> a = e -> { };
        bus.subscribe(a);
        List<Consumer<String>> before = bus.listeners();
        bus.subscribe(e -> { });
        assertThat(before).containsExactly(a);
        assertThatThrownBy(() -> before.add(e -> { })).isInstanceOf(UnsupportedOperationException.class);
        assertThat(bus.listeners()).hasSize(2);
    }
}
