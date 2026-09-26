package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTest {

    /** Builds "https://<host>.example.org" at run time, so no two calls share a String object. */
    private static String origin(String host) {
        return new StringBuilder("https://").append(host).append(".example.org").toString();
    }

    private static List<String> twoOrigins() {
        return new ArrayList<>(List.of(origin("app"), origin("admin")));
    }

    @Test
    void keepsTheOriginsItWasGiven() {
        CorsConfig config = new CorsConfig(twoOrigins());

        assertThat(config.origins()).containsExactly(origin("app"), origin("admin"));
        assertThat(config.allows(origin("app"))).isTrue();
        assertThat(config.allows(origin("admin"))).isTrue();
        assertThat(config.allows(origin("evil"))).isFalse();
    }

    @Test
    void laterChangesToTheCallersListDoNotLeakIn() {
        List<String> given = twoOrigins();
        CorsConfig config = new CorsConfig(given);

        given.add(origin("evil"));
        given.set(0, origin("other"));

        assertThat(config.allows(origin("evil"))).isFalse();
        assertThat(config.allows(origin("other"))).isFalse();
        assertThat(config.origins()).containsExactly(origin("app"), origin("admin"));
    }

    @Test
    void theReturnedListCannotChangeTheConfig() {
        CorsConfig config = new CorsConfig(twoOrigins());

        try {
            config.origins().add(origin("evil"));
        } catch (UnsupportedOperationException refused) {
            // a read-only list is one good answer
        }

        assertThat(config.allows(origin("evil"))).isFalse();
        assertThat(config.origins()).containsExactly(origin("app"), origin("admin"));
    }

    @Test
    void aNullOriginIsRefused() {
        assertThatThrownBy(() -> new CorsConfig(Arrays.asList(origin("app"), null)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CorsConfig(null))
                .isInstanceOf(NullPointerException.class);
    }
}
