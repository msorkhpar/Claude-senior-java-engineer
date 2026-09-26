package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EventLogTest {

    @Test
    void keepsEventsInOrder() throws Exception {
        EventLog log = new EventLog();
        for (int i = 1; i <= 3; i++) {
            log.log("event" + i);
        }
        assertThat(log.events()).containsExactly("event1", "event2", "event3");
        log.log(new String("again"));
        log.log(new String("again"));
        assertThat(log.events()).as("an event logged twice is kept twice")
                .containsExactly("event1", "event2", "event3", "again", "again");
        log.clear();
        assertThat(log.events()).isEmpty();
    }

    @Test
    void aSnapshotCannotBeChanged() throws Exception {
        EventLog log = new EventLog();
        log.log("started");
        List<String> snapshot = log.events();
        assertThatThrownBy(() -> snapshot.add("forged")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.remove(0)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.set(0, "forged")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(log.events()).containsExactly("started");
    }

    @Test
    void aSnapshotIgnoresLaterEvents() throws Exception {
        EventLog log = new EventLog();
        log.log("first");
        List<String> snapshot = log.events();
        log.log("second");
        log.clear();
        log.log("third");
        assertThat(snapshot).containsExactly("first");
        assertThat(log.events()).containsExactly("third");
    }
}
