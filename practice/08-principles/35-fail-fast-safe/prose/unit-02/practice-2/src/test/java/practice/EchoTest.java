package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EchoTest {

    @Test
    void echoesEachElement() {
        List<String> list = new ArrayList<>(List.of("a", "b"));
        Echo.expand(list, "-x");
        assertThat(list).containsExactly("a", "a-x", "b", "b-x");
        List<String> empty = new ArrayList<>();
        Echo.expand(empty, "-x");
        assertThat(empty).isEmpty();
    }

    @Test
    void anElementEndingInTheSuffixIsEchoedToo() {
        List<String> list = new ArrayList<>(List.of("a-x", "b"));
        Echo.expand(list, "-x");
        assertThat(list).containsExactly("a-x", "a-x-x", "b", "b-x");
        List<String> dup = new ArrayList<>(List.of("a", "a-x", "a"));
        Echo.expand(dup, "-x");
        assertThat(dup).containsExactly("a", "a-x", "a-x", "a-x-x", "a", "a-x");
    }
}
