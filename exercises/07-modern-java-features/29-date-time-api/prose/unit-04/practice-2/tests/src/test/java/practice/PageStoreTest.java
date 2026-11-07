package practice;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageStoreTest {

    /** A clock the test moves by hand. Its readings carry nanoseconds, as a system clock's can. */
    static final class HandClock extends Clock {
        private Instant now;

        HandClock(String start) {
            now = Instant.parse(start);
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException(); }
    }

    @Test
    void acceptsAnEditOfTheCurrentVersionAndRefusesOneOfAnOlderVersion() {
        HandClock clock = new HandClock("2026-09-08T14:05:11.104321987Z");
        PageStore store = new PageStore(clock);
        store.create("onboarding", "v1");
        clock.advance(Duration.ofMinutes(3));
        PageStore.Version opened = store.read("onboarding");

        clock.advance(Duration.ofSeconds(40));
        store.save("onboarding", "v2", opened.updatedAt());
        clock.advance(Duration.ofSeconds(25));

        assertThatThrownBy(() -> store.save("onboarding", "v2 from a second tab", opened.updatedAt()))
                .isInstanceOf(PageStore.EditConflict.class);
        assertThat(store.read("onboarding").text()).isEqualTo("v2");
    }

    @Test
    void acceptsTheFirstEditMadeWithTheStampTheCreateReturned() {
        HandClock clock = new HandClock("2026-09-08T14:05:11.104321987Z");
        PageStore store = new PageStore(clock);
        PageStore.Version created = store.create("release-notes", "draft");
        clock.advance(Duration.ofSeconds(90));

        store.save("release-notes", "reviewed", created.updatedAt());

        assertThat(store.read("release-notes").text()).isEqualTo("reviewed");
    }

    @Test
    void givesTwoVersionsSavedWithinOneSecondDifferentStamps() {
        HandClock clock = new HandClock("2026-09-08T14:05:11.104321987Z");
        PageStore store = new PageStore(clock);
        store.create("runbook", "v1");
        PageStore.Version first = store.read("runbook");

        clock.advance(Duration.ofMillis(300));
        PageStore.Version second = store.save("runbook", "v2", first.updatedAt());

        assertThat(second.updatedAt()).isNotEqualTo(first.updatedAt());
    }

    @Test
    void refusesAnEditOfAVersionReplacedWithinTheSameSecond() {
        HandClock clock = new HandClock("2026-09-08T14:05:11.104321987Z");
        PageStore store = new PageStore(clock);
        store.create("runbook", "v1");
        PageStore.Version alice = store.read("runbook");
        PageStore.Version bob = store.read("runbook");

        clock.advance(Duration.ofMillis(300));
        store.save("runbook", "alice's fix", alice.updatedAt());
        clock.advance(Duration.ofMillis(250));

        assertThatThrownBy(() -> store.save("runbook", "bob's fix", bob.updatedAt()))
                .isInstanceOf(PageStore.EditConflict.class);
        assertThat(store.read("runbook").text()).isEqualTo("alice's fix");
    }
}
