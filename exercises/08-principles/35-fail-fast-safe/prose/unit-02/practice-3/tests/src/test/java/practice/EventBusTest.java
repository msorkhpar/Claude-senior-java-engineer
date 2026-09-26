package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

class EventBusTest {

    @Test
    void everyListenerHearsEachEvent() {
        EventBus<String> bus = new EventBus<>();
        List<String> heard = new ArrayList<>();
        bus.subscribe(e -> heard.add("1:" + e));
        bus.subscribe(e -> heard.add("2:" + e));
        bus.publish("x");
        bus.publish("y");
        assertThat(heard).containsExactly("1:x", "2:x", "1:y", "2:y");
        Consumer<String> third = e -> heard.add("3:" + e);
        bus.subscribe(third);
        bus.unsubscribe(third);
        heard.clear();
        bus.publish("z");
        assertThat(heard).containsExactly("1:z", "2:z");
    }

    @Test
    void aListenerMaySubscribeAnotherDuringPublish() {
        EventBus<String> bus = new EventBus<>();
        List<String> heard = new ArrayList<>();
        Consumer<String> late = e -> heard.add("late:" + e);
        bus.subscribe(e -> {
            heard.add("first:" + e);
            if (e.equals("x")) {
                bus.subscribe(late);
            }
        });
        assertThatCode(() -> bus.publish("x")).doesNotThrowAnyException();
        assertThatCode(() -> bus.publish("y")).doesNotThrowAnyException();
        assertThat(heard).contains("first:x", "first:y", "late:y");
    }

    @Test
    void aNewcomerMissesTheEventInFlight() {
        EventBus<String> bus = new EventBus<>();
        List<String> heard = new ArrayList<>();
        Consumer<String> late = e -> heard.add("late:" + e);
        bus.subscribe(e -> {
            heard.add("first:" + e);
            if (e.equals("x")) {
                bus.subscribe(late);
            }
        });
        bus.publish("x");
        bus.publish("y");
        assertThat(heard).containsExactly("first:x", "first:y", "late:y");
    }

    @Test
    void aListenerLeavingMidEventCostsNoOneTheEvent() {
        EventBus<String> bus = new EventBus<>();
        List<String> heard = new ArrayList<>();
        Consumer<String>[] once = new Consumer[1];
        once[0] = e -> {
            heard.add("once:" + e);
            bus.unsubscribe(once[0]);
        };
        bus.subscribe(once[0]);
        bus.subscribe(e -> heard.add("b:" + e));
        bus.subscribe(e -> heard.add("c:" + e));
        bus.publish("x");
        bus.publish("y");
        assertThat(heard).containsExactly("once:x", "b:x", "c:x", "b:y", "c:y");
        EventBus<String> bus2 = new EventBus<>();
        List<String> heard2 = new ArrayList<>();
        Consumer<String> b = e -> heard2.add("b:" + e);
        bus2.subscribe(e -> {
            heard2.add("a:" + e);
            bus2.unsubscribe(b);
        });
        bus2.subscribe(b);
        bus2.publish("x");
        bus2.publish("y");
        assertThat(heard2).containsExactly("a:x", "b:x", "a:y");
    }

    @Test
    void aListenerSubscribedTwiceLeavesOnce() throws Exception {
        EventBus<String> bus = new EventBus<>();
        java.util.List<String> heard = new java.util.ArrayList<>();
        java.util.function.Consumer<String> twice = heard::add;
        bus.subscribe(twice);
        bus.subscribe(twice);
        bus.publish("a");
        assertThat(heard).containsExactly("a", "a");
        bus.unsubscribe(twice);
        bus.publish("b");
        assertThat(heard).containsExactly("a", "a", "b");
    }
}
