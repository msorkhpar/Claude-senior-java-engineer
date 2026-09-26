package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamesTest {

    private static List<String> untrimmed() {
        return new ArrayList<>(List.of("  ada ", "bob", " cy"));
    }

    @Test
    void trimmedCopyTrimsEveryName() {
        assertThat(Names.trimmedCopy(untrimmed())).containsExactly("ada", "bob", "cy");
    }

    @Test
    void trimAllChangesTheCallersList() {
        List<String> mine = untrimmed();

        Names.trimAll(mine);

        assertThat(mine).containsExactly("ada", "bob", "cy");
    }

    @Test
    void trimmedCopyLeavesTheCallersListAlone() {
        List<String> mine = untrimmed();

        List<String> result = Names.trimmedCopy(mine);

        assertThat(mine).containsExactly("  ada ", "bob", " cy");
        assertThat(result).isNotSameAs(mine).containsExactly("ada", "bob", "cy");

        List<String> clean = new java.util.ArrayList<>(List.of("ada", "bob"));
        List<String> copy = Names.trimmedCopy(clean);
        assertThat(copy).isNotSameAs(clean).containsExactly("ada", "bob");
    }
}
