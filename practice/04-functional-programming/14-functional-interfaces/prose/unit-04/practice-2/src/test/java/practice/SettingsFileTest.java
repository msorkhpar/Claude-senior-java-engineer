package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsFileTest {

    private static final List<String> PLAIN = List.of(
            "# Mail relay for the billing service",
            "# Owner: payments team",
            "relay.host=127.0.0.1",
            "relay.port=2525",
            "relay.tls=false");

    private static final List<String> WITH_DISABLED = List.of(
            "# Thumbnail cache",
            "cache.size=512",
            "# cache.ttl=3600",
            "cache.dir=/path/to/project/cache",
            "#cache.warmup=true");

    @Test
    void splitsAFileIntoItsDescriptionAndItsSettings() {
        assertThat(SettingsFile.description(PLAIN))
                .containsExactly("# Mail relay for the billing service", "# Owner: payments team");
        assertThat(SettingsFile.settings(PLAIN))
                .containsExactly("relay.host=127.0.0.1", "relay.port=2525", "relay.tls=false");
    }

    @Test
    void aFileWithoutADescriptionIsAllSettings() {
        List<String> bare = List.of("queue.workers=4", "queue.retries=2");

        assertThat(SettingsFile.description(bare)).isEmpty();
        assertThat(SettingsFile.settings(bare)).containsExactly("queue.workers=4", "queue.retries=2");
    }

    @Test
    void aCommentedOutSettingIsNotPartOfTheDescription() {
        assertThat(SettingsFile.description(WITH_DISABLED)).containsExactly("# Thumbnail cache");
    }

    @Test
    void theSettingsSectionKeepsItsCommentedOutLines() {
        assertThat(SettingsFile.settings(WITH_DISABLED)).containsExactly(
                "cache.size=512", "# cache.ttl=3600", "cache.dir=/path/to/project/cache", "#cache.warmup=true");
    }
}
