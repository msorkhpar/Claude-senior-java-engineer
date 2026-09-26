package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuardedSubjectTest {

    @Test
    void publishReachesEveryObserver() {
        GuardedSubject subject = new GuardedSubject();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        subject.addObserver(first::add);
        subject.addObserver(second::add);

        assertThat(subject.publish("temp")).isTrue();
        assertThat(subject.publish("humidity")).isTrue();

        assertThat(first).containsExactly("temp", "humidity");
        assertThat(second).containsExactly("temp", "humidity");
    }

    @Test
    void aPublishDuringNotificationIsIgnored() {
        GuardedSubject subject = new GuardedSubject();
        List<String> before = new ArrayList<>();
        List<String> seenByAdjuster = new ArrayList<>();
        List<Boolean> nestedResults = new ArrayList<>();
        List<String> after = new ArrayList<>();
        subject.addObserver(before::add);
        subject.addObserver(event -> {
            seenByAdjuster.add(event);
            if (event.equals("temp")) {
                nestedResults.add(subject.publish("adjust"));
            }
        });
        subject.addObserver(after::add);

        assertThat(subject.publish("temp")).isTrue();

        assertThat(nestedResults).containsExactly(false);
        assertThat(before).containsExactly("temp");
        assertThat(seenByAdjuster).containsExactly("temp");
        assertThat(after).containsExactly("temp");
    }

    @Test
    void theGuardIsClearedWhenAnObserverThrows() {
        GuardedSubject subject = new GuardedSubject();
        List<String> got = new ArrayList<>();
        subject.addObserver(event -> {
            if (event.equals("bad")) {
                throw new IllegalStateException("cannot handle " + event);
            }
        });
        subject.addObserver(got::add);

        assertThatThrownBy(() -> subject.publish("bad")).isInstanceOf(IllegalStateException.class);
        assertThat(subject.publish("ok")).isTrue();

        assertThat(got).containsExactly("ok");
    }
}
