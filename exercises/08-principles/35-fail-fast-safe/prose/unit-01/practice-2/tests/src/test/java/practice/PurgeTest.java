package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PurgeTest {

    @Test
    void removesEveryMatch() {
        List<String> items = new ArrayList<>(List.of("keep", "drop", "keep2", "drop"));
        Purge.removeAll(items, "drop");
        assertThat(items).containsExactly("keep", "keep2");
        List<String> none = new ArrayList<>(List.of("x", "y"));
        Purge.removeAll(none, "drop");
        assertThat(none).containsExactly("x", "y");
    }

    @Test
    void adjacentMatchesAreAllRemoved() {
        List<String> items = new ArrayList<>(List.of("drop", "drop", "keep", "drop", "drop"));
        Purge.removeAll(items, "drop");
        assertThat(items).containsExactly("keep");
    }

    @Test
    void textBuiltAtRunTimeMatches() {
        String built = new StringBuilder("dr").append("op").toString();
        List<String> items = new ArrayList<>(List.of("keep"));
        items.add(new String("drop"));
        items.add(built);
        Purge.removeAll(items, new String("drop"));
        assertThat(items).containsExactly("keep");
    }

    @Test
    void nullElementsStay() {
        List<String> items = new ArrayList<>();
        items.add("a");
        items.add(null);
        items.add("drop");
        Purge.removeAll(items, "drop");
        assertThat(items).containsExactly("a", null);
    }

    @Test
    void onlyAnExactMatchIsRemoved() {
        List<String> items = new ArrayList<>(List.of("Drop", "drop ", "BB", "drop"));
        Purge.removeAll(items, new StringBuilder("dr").append("op").toString());
        assertThat(items).containsExactly("Drop", "drop ", "BB");
        // "Aa" and "BB" share a hash code, but not their characters
        List<String> twins = new ArrayList<>(List.of("BB", "Aa"));
        Purge.removeAll(twins, new String("Aa"));
        assertThat(twins).containsExactly("BB");
    }
}
