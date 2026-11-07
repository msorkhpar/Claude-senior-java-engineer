package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiskAlarmTest {

    @Test
    void anAlarmRaisesForItsOwnVolumeAtOrAboveTheLimit() {
        DiskAlarm.Feed feed = new DiskAlarm.Feed();
        DiskAlarm alarm = DiskAlarm.watch(feed, "/home", 80);

        feed.publish("/home", 50);
        feed.publish("/tmp", 95);
        feed.publish("/home", 80);
        feed.publish("/home", 86);

        assertThat(alarm.raised()).containsExactly("/home at 80%", "/home at 86%");
    }

    @Test
    void aWatchStartedAfterAHighReadingRaisesItAtOnce() {
        DiskAlarm.Feed feed = new DiskAlarm.Feed();
        feed.publish("/var", 97);
        feed.publish("/opt", 12);

        DiskAlarm alarm = DiskAlarm.watch(feed, "/var", 90);

        assertThat(alarm.raised()).containsExactly("/var at 97%");
    }

    @Test
    void aRefusedWatchLeavesNothingListeningOnTheFeed() {
        DiskAlarm.Feed feed = new DiskAlarm.Feed();
        feed.publish("/srv", 40);

        assertThatThrownBy(() -> DiskAlarm.watch(feed, "/srv", 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(feed.listeners()).isZero();
    }
}
