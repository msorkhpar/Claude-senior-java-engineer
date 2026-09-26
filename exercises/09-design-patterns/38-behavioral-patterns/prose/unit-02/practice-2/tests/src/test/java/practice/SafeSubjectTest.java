package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import practice.SafeSubject.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafeSubjectTest {

    /** Records every event it gets as "event:data". */
    static final class Recorder implements Observer<String> {
        final List<String> got = new ArrayList<>();

        @Override
        public void update(String event, String data) {
            got.add(event + ":" + data);
        }
    }

    @Test
    void everyObserverGetsTheEvent() {
        SafeSubject<String> subject = new SafeSubject<>();
        Recorder a = new Recorder();
        Recorder b = new Recorder();
        subject.addObserver(a);
        subject.addObserver(b);

        List<RuntimeException> failures = subject.notifyObservers("PRICE", "101.5");
        subject.removeObserver(b);
        subject.notifyObservers("PRICE", "99.0");

        assertThat(failures).isEmpty();
        assertThat(a.got).containsExactly("PRICE:101.5", "PRICE:99.0");
        assertThat(b.got).containsExactly("PRICE:101.5");
        assertThat(new SafeSubject<String>().notifyObservers("PRICE", "1")).isEmpty();
        assertThatThrownBy(() -> subject.addObserver(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anObserverThatUnsubscribesDuringNotificationDoesNotBreakTheOthers() {
        SafeSubject<String> subject = new SafeSubject<>();
        Recorder a = new Recorder();
        Recorder b = new Recorder();
        List<String> seenByRemover = new ArrayList<>();
        Observer<String> remover = new Observer<>() {
            @Override
            public void update(String event, String data) {
                seenByRemover.add(data);
                subject.removeObserver(this);
            }
        };
        subject.addObserver(a);
        subject.addObserver(remover);
        subject.addObserver(b);

        List<RuntimeException> first = subject.notifyObservers("E", "one");
        List<RuntimeException> second = subject.notifyObservers("E", "two");

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        assertThat(a.got).containsExactly("E:one", "E:two");
        assertThat(b.got).containsExactly("E:one", "E:two");
        assertThat(seenByRemover).containsExactly("one");
    }

    @Test
    void anObserverAddedDuringNotificationWaitsForTheNextEvent() {
        SafeSubject<String> subject = new SafeSubject<>();
        Recorder newcomer = new Recorder();
        Recorder b = new Recorder();
        boolean[] added = {false};
        subject.addObserver((event, data) -> {
            if (!added[0]) {
                added[0] = true;
                subject.addObserver(newcomer);
            }
        });
        subject.addObserver(b);

        subject.notifyObservers("E", "one");
        subject.notifyObservers("E", "two");

        assertThat(b.got).containsExactly("E:one", "E:two");
        assertThat(newcomer.got).containsExactly("E:two");
    }

    @Test
    void aFailingObserverDoesNotStopTheOthers() {
        SafeSubject<String> subject = new SafeSubject<>();
        IllegalStateException boom = new IllegalStateException("boom");
        Recorder b = new Recorder();
        subject.addObserver((event, data) -> {
            throw boom;
        });
        subject.addObserver(b);

        List<RuntimeException> first = subject.notifyObservers("E", "one");
        List<RuntimeException> second = subject.notifyObservers("E", "two");

        assertThat(b.got).containsExactly("E:one", "E:two");
        assertThat(first).containsExactly(boom);
        assertThat(second).containsExactly(boom);
    }
}
