package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PurgeTest {

    private static String word(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void removesEveryMatch() {
        String b = word("b");
        List<String> list = new ArrayList<>(List.of(word("a"), b, word("c"), b, word("d")));
        assertThat(Purge.removeAll(list, b)).isEqualTo(2);
        assertThat(list).containsExactly("a", "c", "d");
        List<String> none = new ArrayList<>(List.of(word("a"), word("c")));
        assertThat(Purge.removeAll(none, b)).isZero();
        assertThat(none).containsExactly("a", "c");
    }

    @Test
    void adjacentMatchesBothRemoved() {
        String b = word("b");
        List<String> list = new ArrayList<>(List.of(b, b, word("a"), b, b));
        assertThat(Purge.removeAll(list, b)).isEqualTo(4);
        assertThat(list).containsExactly("a");
    }

    @Test
    void equalTextIsRemoved() {
        List<String> list = new ArrayList<>(List.of(word("a"), word("b"), word("c"), word("b")));
        String target = word("b");
        assertThat(Purge.removeAll(list, target)).isEqualTo(2);
        assertThat(list).containsExactly("a", "c");
    }

    @Test
    void worksOnACopyOnWriteList() {
        String b = word("b");
        List<String> list = new CopyOnWriteArrayList<>(List.of(word("a"), b, word("c"), b));
        assertThat(Purge.removeAll(list, b)).isEqualTo(2);
        assertThat(list).containsExactly("a", "c");
    }
}
