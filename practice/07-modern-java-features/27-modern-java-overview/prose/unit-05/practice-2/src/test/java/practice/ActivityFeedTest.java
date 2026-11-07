package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActivityFeedTest {

    private static final Duration LIMIT = Duration.ofSeconds(10);

    private static ActivityFeed feedWith(String... events) {
        ActivityFeed feed = new ActivityFeed();
        for (String e : events) feed.record(e);
        return feed;
    }

    @Test
    void bothListsShowTheNewestEventFirst() {
        assertTimeoutPreemptively(LIMIT, () -> {
            ActivityFeed feed = feedWith("Jane Doe joined", "board renamed", "card moved", "comment added");
            assertThat(feed.live()).containsExactly("comment added", "card moved", "board renamed", "Jane Doe joined");
            assertThat(feed.latest(2)).containsExactly("comment added", "card moved");
            assertThat(feed.latest(9)).containsExactly("comment added", "card moved", "board renamed", "Jane Doe joined");
        });
    }

    @Test
    void theDashboardCannotChangeTheFeed() {
        assertTimeoutPreemptively(LIMIT, () -> {
            ActivityFeed feed = feedWith("file uploaded", "file shared");
            List<String> dashboard = feed.live();
            Throwable thrown = catchThrowable(() -> dashboard.add("forged event"));
            assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
            assertThat(feed.history()).containsExactly("file uploaded", "file shared");
        });
    }

    @Test
    void theDashboardSeesEventsRecordedAfterItGotItsList() {
        assertTimeoutPreemptively(LIMIT, () -> {
            ActivityFeed feed = feedWith("sprint started");
            List<String> dashboard = feed.live();
            feed.record("task closed");
            feed.record("sprint ended");
            assertThat(dashboard).containsExactly("sprint ended", "task closed", "sprint started");
        });
    }

    @Test
    void theDigestKeepsWhatItHadWhenItWasBuilt() {
        assertTimeoutPreemptively(LIMIT, () -> {
            ActivityFeed feed = feedWith("invoice 31 sent", "invoice 32 sent", "invoice 31 paid");
            List<String> digest = feed.latest(2);
            feed.record("invoice 33 sent");
            assertThat(digest).containsExactly("invoice 31 paid", "invoice 32 sent");
        });
    }
}
