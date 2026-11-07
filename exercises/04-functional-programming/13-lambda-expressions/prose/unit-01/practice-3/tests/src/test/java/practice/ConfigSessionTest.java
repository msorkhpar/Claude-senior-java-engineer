package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigSessionTest {

    private final ConfigSession.ConfigWatcher watcher = new ConfigSession.ConfigWatcher();

    @Test
    void anOpenSessionHearsEveryChange() {
        ConfigSession session = new ConfigSession(watcher);
        session.open();

        watcher.changed("smtp.relay");
        watcher.changed("quota.daily");

        assertThat(session.heard()).containsExactly("smtp.relay", "quota.daily");
    }

    @Test
    void twoOpenSessionsBothHear() {
        ConfigSession first = new ConfigSession(watcher);
        ConfigSession second = new ConfigSession(watcher);
        first.open();
        second.open();

        watcher.changed("feature.dark-mode");

        assertThat(first.heard()).containsExactly("feature.dark-mode");
        assertThat(second.heard()).containsExactly("feature.dark-mode");
    }

    @Test
    void aClosedSessionHearsNothingAndLeavesNothingBehind() {
        ConfigSession session = new ConfigSession(watcher);
        session.open();
        watcher.changed("smtp.relay");

        session.close();
        watcher.changed("quota.daily");

        assertThat(session.heard()).containsExactly("smtp.relay");
        assertThat(watcher.listenerCount()).isZero();
    }

    @Test
    void closingOneSessionLeavesTheOthersListening() {
        ConfigSession first = new ConfigSession(watcher);
        ConfigSession second = new ConfigSession(watcher);
        first.open();
        second.open();

        first.close();
        watcher.changed("cache.ttl");

        assertThat(first.heard()).isEmpty();
        assertThat(second.heard()).containsExactly("cache.ttl");
        assertThat(watcher.listenerCount()).isEqualTo(1);
    }
}
