package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DependenciesTest {

    private static Dependencies graph(String... pairs) {
        Dependencies d = new Dependencies();
        for (int i = 0; i < pairs.length; i += 2) {
            d.addEdge(pairs[i], pairs[i + 1]);
        }
        return d;
    }

    @Test
    void findsThePageCycle() {
        assertThat(graph("A", "B", "B", "C", "C", "A").hasCycle()).isTrue();
        assertThat(graph("A", "B", "B", "C").hasCycle()).isFalse();
    }

    @Test
    void aDiamondIsNotACycle() {
        assertThat(graph("A", "B", "A", "C", "B", "D", "C", "D").hasCycle()).isFalse();
    }

    @Test
    void twoVerticesPointingAtEachOtherAreACycle() {
        assertThat(graph("A", "B", "B", "A").hasCycle()).isTrue();
    }

    @Test
    void aCycleAwayFromTheFirstVertexIsFound() {
        assertThat(graph("A", "B", "X", "Y", "Y", "Z", "Z", "X").hasCycle()).isTrue();
    }
}
