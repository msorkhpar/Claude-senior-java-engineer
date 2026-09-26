package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SimpleEventBusTest {

    @Test
    void deliversToEachListenerOfTheType() throws Exception {
        SimpleEventBus bus = new SimpleEventBus();
        List<String> seen = new ArrayList<>();
        bus.subscribe("order", e -> seen.add("a:" + e));
        bus.subscribe("order", e -> seen.add("b:" + e));
        bus.subscribe("user", e -> seen.add("user:" + e));
        bus.publish("order", "o-1");
        assertThat(seen).containsExactly("a:o-1", "b:o-1");
        assertThat(bus.listenerCount("order")).isEqualTo(2);
        assertThat(bus.listenerCount("user")).isEqualTo(1);
    }

    @Test
    void aTypeWithNoListenersIsANoOp() throws Exception {
        SimpleEventBus bus = new SimpleEventBus();
        assertThatCode(() -> bus.publish("nobody", "x")).doesNotThrowAnyException();
        assertThat(bus.listenerCount("nobody")).isZero();
    }

    @Test
    void aListenerMaySubscribeWhilePublishing() throws Exception {
        SimpleEventBus bus = new SimpleEventBus();
        List<String> seen = new ArrayList<>();
        bus.subscribe("order", e -> {
            seen.add("first:" + e);
            if (seen.size() == 1) {
                bus.subscribe("order", later -> seen.add("late:" + later));
            }
        });
        assertThatCode(() -> bus.publish("order", "o-1")).doesNotThrowAnyException();
        assertThat(seen).containsExactly("first:o-1");
        bus.publish("order", "o-2");
        assertThat(seen).containsExactly("first:o-1", "first:o-2", "late:o-2");
    }

    @Test
    void theSameListenerTwiceIsTwoSubscriptions() throws Exception {
        SimpleEventBus bus = new SimpleEventBus();
        java.util.List<Object> heard = new java.util.ArrayList<>();
        java.util.function.Consumer<Object> twice = heard::add;
        bus.subscribe("order", twice);
        bus.subscribe("order", twice);
        assertThat(bus.listenerCount("order")).isEqualTo(2);
        bus.publish("order", "o-1");
        assertThat(heard).containsExactly("o-1", "o-1");
    }
}
