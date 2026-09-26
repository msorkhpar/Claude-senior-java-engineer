package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class TemperatureAlarmTest {

    /** A source that keeps its listeners and, if asked, replays one reading to each new listener. */
    private static final class Source implements TemperatureAlarm.EventSource {
        private final List<IntConsumer> listeners = new ArrayList<>();
        private final Integer replay;

        Source(Integer replay) {
            this.replay = replay;
        }

        @Override
        public void register(IntConsumer listener) {
            listeners.add(listener);
            if (replay != null) {
                listener.accept(replay);
            }
        }

        void deliver(int... readings) {
            for (int r : readings) {
                listeners.forEach(l -> l.accept(r));
            }
        }
    }

    @Test
    void countsReadingsAboveTheThreshold() {
        Source source = new Source(null);
        TemperatureAlarm alarm = TemperatureAlarm.create(30, source);
        assertThat(alarm.threshold()).isEqualTo(30);
        assertThat(alarm.alarms()).isZero();
        source.deliver(25, 31, 40, 30);
        assertThat(alarm.alarms()).isEqualTo(2);
    }

    @Test
    void aReadingDuringRegistrationSeesTheThreshold() {
        Source source = new Source(25);
        TemperatureAlarm alarm = TemperatureAlarm.create(30, source);
        assertThat(alarm.alarms()).as("25 is not above 30").isZero();
        source.deliver(35);
        assertThat(alarm.alarms()).isEqualTo(1);
    }

    @Test
    void everyFieldIsFinal() {
        TemperatureAlarm.create(30, new Source(null));
        List<Field> fields = Arrays.stream(TemperatureAlarm.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
        assertThat(fields).isNotEmpty();
        assertThat(fields).allSatisfy(f -> assertThat(Modifier.isFinal(f.getModifiers()))
                .as("field %s is final", f.getName()).isTrue());
    }
}
