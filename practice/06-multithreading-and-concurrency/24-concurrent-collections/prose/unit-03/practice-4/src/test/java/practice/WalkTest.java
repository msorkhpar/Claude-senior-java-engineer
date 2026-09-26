package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WalkTest {

    private static CopyOnWriteArrayList<String> abc() {
        return new CopyOnWriteArrayList<>(List.of(new String("a".toCharArray()),
                new String("b".toCharArray()), new String("c".toCharArray())));
    }

    @Test
    void visitsEveryElement() {
        CopyOnWriteArrayList<String> list = abc();
        List<String> seen = new ArrayList<>();
        List<String> visited = Walk.visitAll(list, seen::add);
        assertThat(visited).containsExactly("a", "b", "c");
        assertThat(seen).containsExactly("a", "b", "c");
        assertThat(list).containsExactly("a", "b", "c");
    }

    @Test
    void addedDuringTheWalkNotVisited() {
        CopyOnWriteArrayList<String> list = abc();
        List<String> visited = Walk.visitAll(list, s -> list.add("x" + s));
        assertThat(visited).containsExactly("a", "b", "c");
        assertThat(list).containsExactly("a", "b", "c", "xa", "xb", "xc");
    }

    @Test
    void removedDuringTheWalkStillVisited() {
        CopyOnWriteArrayList<String> list = abc();
        List<String> visited = Walk.visitAll(list, s -> {
            if (s.equals("a")) {
                list.remove("c");
            }
        });
        assertThat(visited).containsExactly("a", "b", "c");
        assertThat(list).containsExactly("a", "b");
    }
}
