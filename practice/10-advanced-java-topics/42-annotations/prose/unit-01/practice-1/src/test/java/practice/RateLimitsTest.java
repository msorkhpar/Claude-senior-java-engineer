package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RateLimitsTest {

    /** A parsed tree: each route maps to a mutable list, as a parser builds it. */
    private static Map<String, Object> tree(Object... routeAndNode) {
        Map<String, Object> raw = new LinkedHashMap<>();
        for (int i = 0; i < routeAndNode.length; i += 2) raw.put((String) routeAndNode[i], routeAndNode[i + 1]);
        return raw;
    }

    private static List<Object> parsed(Object... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    @Test
    void loadsWellFormedLimits() {
        RateLimits limits = RateLimits.load(tree(
                "/search", parsed(120, 900),
                "/upload", parsed(6, 45, 300)));
        assertThat(limits.perMinute("/search", 0)).isEqualTo(120);
        assertThat(limits.perMinute("/upload", 2)).isEqualTo(300);
        assertThat(limits.capacity("/upload")).isEqualTo(351);
    }

    @Test
    void refusesARouteWhoseNodeIsNotAList() {
        assertThatThrownBy(() -> RateLimits.load(tree("/export", "15")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("/export");
    }

    @Test
    void refusesAQuotedLimitWhenTheConfigLoads() {
        assertThatThrownBy(() -> RateLimits.load(tree(
                "/search", parsed(120, 900),
                "/login", parsed("20", 60))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("/login");
    }

    @Test
    void refusesABadLimitAfterGoodOnes() {
        assertThatThrownBy(() -> RateLimits.load(tree(
                "/upload", parsed(6, 45, 2.5))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("/upload");
    }

    @Test
    void laterChangesToTheParsedTreeDoNotReachTheLimits() {
        List<Object> search = parsed(120, 900);
        RateLimits limits = RateLimits.load(tree("/search", search));
        search.set(0, "unlimited");
        search.add(5000);
        assertThat(limits.perMinute("/search", 0)).isEqualTo(120);
        assertThat(limits.capacity("/search")).isEqualTo(1020);
    }
}
