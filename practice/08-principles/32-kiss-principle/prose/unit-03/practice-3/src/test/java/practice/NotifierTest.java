package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

class NotifierTest {

    @Test
    void callsEveryListenerInOrder() throws Exception {
        Notifier<String> notifier = new Notifier<>();
        List<String> received = new ArrayList<>();
        notifier.publish("nobody listens");
        notifier.addListener(event -> received.add("1:" + event));
        notifier.addListener(event -> received.add("2:" + event.toUpperCase()));
        notifier.publish("hello");
        assertThat(received).containsExactly("1:hello", "2:HELLO");
        Notifier<String> twice = new Notifier<>();
        List<String> heard = new ArrayList<>();
        Consumer<String> listener = event -> heard.add(event);
        twice.addListener(listener);
        twice.addListener(listener);
        twice.publish("ping");
        assertThat(heard).as("a listener added twice hears each event twice").containsExactly("ping", "ping");
    }

    @Test
    void aListenerMayAddAnotherDuringPublish() throws Exception {
        Notifier<String> notifier = new Notifier<>();
        List<String> received = new ArrayList<>();
        notifier.addListener(event -> {
            received.add("first:" + event);
            if (event.equals("one")) {
                notifier.addListener(later -> received.add("late:" + later));
            }
        });
        assertThatCode(() -> notifier.publish("one")).doesNotThrowAnyException();
        notifier.publish("two");
        assertThat(received).containsExactly("first:one", "first:two", "late:two");
    }

    @Test
    void aListenerFailureReachesTheCaller() throws Exception {
        Notifier<String> notifier = new Notifier<>();
        List<String> received = new ArrayList<>();
        IllegalArgumentException broke = new IllegalArgumentException("listener broke");
        notifier.addListener(event -> {
            throw broke;
        });
        notifier.addListener(event -> received.add(event));
        Throwable thrown = catchThrowable(() -> notifier.publish("order-placed"));
        assertThat(thrown).as("the listener's own exception, not a wrapper").isSameAs(broke);
        assertThat(received).isEmpty();
    }
}
