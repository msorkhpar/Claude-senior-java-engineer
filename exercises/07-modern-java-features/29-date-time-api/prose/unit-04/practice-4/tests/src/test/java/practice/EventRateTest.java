package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class EventRateTest {

    private static Instant at(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    private static Map.Entry<Instant, Integer> second(long second, int count) {
        return entry(Instant.ofEpochSecond(second), count);
    }

    @Test
    void countsEventsPerSecond() {
        assertThat(EventRate.perSecond(List.of(at(100_100), at(100_400), at(101_200))))
                .containsExactly(second(100, 2), second(101, 1));
        assertThat(EventRate.perSecond(List.of(at(105_300), at(102_100))))
                .containsExactly(second(102, 1), second(105, 1));
    }

    @Test
    void truncatesNeverRounds() {
        assertThat(EventRate.perSecond(List.of(at(100_400), at(100_600))))
                .containsExactly(second(100, 2));
        assertThat(EventRate.perSecond(List.of(at(100_999), at(101_001))))
                .containsExactly(second(100, 1), second(101, 1));
    }

    @Test
    void beforeTheEpochIsItsOwnSecond() {
        assertThat(EventRate.perSecond(List.of(at(-500), at(200))))
                .containsExactly(second(-1, 1), second(0, 1));
    }
}
