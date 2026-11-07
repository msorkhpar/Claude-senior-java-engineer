package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetrySettingsTest {

    /** What the service's settings loader does: no-argument constructor, then each named field. */
    static RetrySettings load(Map<String, Object> document) throws Exception {
        Constructor<RetrySettings> constructor = RetrySettings.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        RetrySettings settings = constructor.newInstance();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            Field field = RetrySettings.class.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(settings, entry.getValue());
        }
        return settings;
    }

    @Test
    void theLoaderBuildsSettingsFromACompleteDocument() throws Exception {
        RetrySettings s = load(Map.of("endpoint", "https://ledger.example.com",
                "maxAttempts", 5, "backoffMillis", 750L));

        assertThat(s.endpoint()).isEqualTo("https://ledger.example.com");
        assertThat(s.maxAttempts()).isEqualTo(5);
        assertThat(s.backoffMillis()).isEqualTo(750L);
    }

    @Test
    void theShortFormKeepsTheEndpointAndUsesTheDefaults() {
        RetrySettings s = new RetrySettings("https://fx.example.com");

        assertThat(s.endpoint()).isEqualTo("https://fx.example.com");
        assertThat(s.maxAttempts()).isEqualTo(3);
        assertThat(s.backoffMillis()).isEqualTo(200L);
    }

    @Test
    void aDocumentThatNamesOnlyTheEndpointGetsTheDefaultsForTheRest() throws Exception {
        RetrySettings s = load(Map.of("endpoint", "https://fx.example.com"));

        assertThat(s.maxAttempts()).isEqualTo(3);
        assertThat(s.backoffMillis()).isEqualTo(200L);
    }

    @Test
    void theShortFormRefusesABlankEndpoint() {
        assertThatThrownBy(() -> new RetrySettings("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theShortFormStoresTheEndpointInTheSameFormAsTheFullOne() {
        RetrySettings shortForm = new RetrySettings("  HTTPS://Ledger.Example.com/ ");
        RetrySettings fullForm = new RetrySettings("  HTTPS://Ledger.Example.com/ ", 3, 200);

        assertThat(shortForm.endpoint()).isEqualTo(fullForm.endpoint()).isEqualTo("https://ledger.example.com");
    }
}
