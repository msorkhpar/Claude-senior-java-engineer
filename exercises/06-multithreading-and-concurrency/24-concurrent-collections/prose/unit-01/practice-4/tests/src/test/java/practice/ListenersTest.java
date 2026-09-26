package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ListenersTest {

    /** A listener that records every event it hears. */
    private static final class Inbox implements Consumer<String> {
        final List<String> heard = new ArrayList<>();

        @Override
        public void accept(String event) {
            heard.add(event);
        }
    }

    @Test
    void firesEveryListener() {
        Listeners registry = new Listeners();
        List<String> order = new ArrayList<>();
        Consumer<String> a = e -> order.add("a:" + e);
        Consumer<String> b = e -> order.add("b:" + e);
        assertThat(registry.register(a)).isTrue();
        assertThat(registry.register(b)).isTrue();
        assertThat(registry.fire("x")).isEqualTo(2);
        assertThat(order).containsExactly("a:x", "b:x");
        assertThat(registry.unregister(a)).isTrue();
        assertThat(registry.unregister(a)).isFalse();
        assertThat(registry.fire("y")).isEqualTo(1);
        assertThat(order).containsExactly("a:x", "b:x", "b:y");
    }

    @Test
    void refusesADuplicate() {
        Listeners registry = new Listeners();
        Inbox a = new Inbox();
        assertThat(registry.register(a)).isTrue();
        assertThat(registry.register(a)).isFalse();
        assertThat(registry.fire("x")).isEqualTo(1);
        assertThat(a.heard).containsExactly("x");
    }

    @Test
    void aListenerAddedDuringFireWaitsForTheNextEvent() {
        Listeners registry = new Listeners();
        Inbox b = new Inbox();
        Consumer<String> a = e -> registry.register(b);
        registry.register(a);
        assertThat(registry.fire("1")).isEqualTo(1);
        assertThat(b.heard).isEmpty();
        assertThat(registry.fire("2")).isEqualTo(2);
        assertThat(b.heard).containsExactly("2");
    }

    @Test
    void aListenerMayRemoveItselfDuringFire() {
        Listeners registry = new Listeners();
        Inbox b = new Inbox();
        Consumer<String>[] self = new Consumer[1];
        self[0] = e -> registry.unregister(self[0]);
        registry.register(self[0]);
        registry.register(b);
        assertThat(registry.fire("1")).isEqualTo(2);
        assertThat(b.heard).containsExactly("1");
        assertThat(registry.fire("2")).isEqualTo(1);
        assertThat(b.heard).containsExactly("1", "2");
    }
}
