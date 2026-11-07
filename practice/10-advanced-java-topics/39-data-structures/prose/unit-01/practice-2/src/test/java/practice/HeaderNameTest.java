package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderNameTest {

    /** "Ea" and "FB" have the same String hashCode, so every name built from k such blocks does too. */
    private static List<String> collidingNames(int blocks) {
        List<String> names = new ArrayList<>(List.of("X-"));
        for (int i = 0; i < blocks; i++) {
            List<String> next = new ArrayList<>();
            for (String n : names) { next.add(n + "Ea"); next.add(n + "FB"); }
            names = next;
        }
        return names;
    }

    private static HeaderName fresh(String name) {
        return new HeaderName(new String(name));       // equal to the stored key, never the same object
    }

    @Test
    void storesAndFindsHeadersAndListsThemSorted() {
        Map<HeaderName, String> headers = new HashMap<>();
        List<String> plain = List.of("Host", "Accept", "Via", "Cookie", "X-Forwarded-For");
        for (String name : plain) headers.put(fresh(name), "v:" + name);
        List<String> crafted = collidingNames(6);
        for (String name : crafted) headers.put(fresh(name), "v:" + name);

        for (String name : plain) assertThat(headers.get(fresh(name))).isEqualTo("v:" + name);
        for (String name : crafted) assertThat(headers.get(fresh(name))).isEqualTo("v:" + name);
        assertThat(headers.get(fresh("Referer"))).isNull();

        TreeSet<HeaderName> log = new TreeSet<>();
        for (String name : plain) log.add(fresh(name));
        assertThat(log).extracting(HeaderName::value)
                .containsExactlyInAnyOrder("Accept", "Cookie", "Host", "Via", "X-Forwarded-For");
    }

    @Test
    void lookupsStayCheapWhenEveryNameHasTheSameHashCode() {
        List<String> crafted = collidingNames(12);                 // 4,096 names, one hash code
        assertThat(crafted.stream().mapToInt(String::hashCode).distinct().count()).isEqualTo(1);
        Map<HeaderName, Integer> headers = new HashMap<>();
        for (int i = 0; i < crafted.size(); i++) headers.put(fresh(crafted.get(i)), i);

        HeaderName.EQUALS_CALLS.set(0);
        for (int i = 0; i < crafted.size(); i++) assertThat(headers.get(fresh(crafted.get(i)))).isEqualTo(i);
        double perLookup = (double) HeaderName.EQUALS_CALLS.get() / crafted.size();

        assertThat(perLookup).as("equals() calls per lookup").isLessThan(64);
    }

    @Test
    void namesThatDifferOnlyInCaseStayTwoHeaders() {
        TreeSet<HeaderName> log = new TreeSet<>();
        for (String name : List.of("X-Trace-Id", "x-trace-id", "Etag", "ETag")) log.add(fresh(name));

        assertThat(log).extracting(HeaderName::value)
                .containsExactlyInAnyOrder("X-Trace-Id", "x-trace-id", "Etag", "ETag");
    }
}
