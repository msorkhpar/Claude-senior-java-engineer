package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DedupeTest {

    @Test
    void dropsRepeatedValues() {
        Point p = new Point(1, 2);
        Point q = new Point(3, 4);

        assertThat(Dedupe.distinctPoints(List.of(p, q, p))).containsExactly(p, q);
        assertThat(Dedupe.distinctPoints(List.of())).isEmpty();
        assertThat(Dedupe.firstSeen(List.of("a", "b", "a", "c", "b"))).containsExactly("a", "b", "c");
    }

    @Test
    void equalPointsCollapse() {
        List<Point> unique = Dedupe.distinctPoints(List.of(new Point(1, 2), new Point(3, 4), new Point(1, 2)));

        assertThat(unique).hasSize(2);
        assertThat(unique.get(0).x()).isEqualTo(1);
        assertThat(unique.get(1).y()).isEqualTo(4);
        assertThat(Dedupe.distinctPoints(List.of(new Point(1, 2), new Point(1, 5), new Point(4, 2)))).hasSize(3);
    }

    @Test
    void firstSeenOrderIsKept() {
        assertThat(Dedupe.firstSeen(List.of("pear", "apple", "pear", "fig"))).containsExactly("pear", "apple", "fig");
    }

    @Test
    void firstSeenComparesByValue() {
        List<String> names = List.of(new String("pear"), new String("apple"), new String("pear"), new String("apple"));

        assertThat(Dedupe.firstSeen(names)).containsExactly("pear", "apple");
    }

    @Test
    void equalsNeedsAMatchingHashCode() {
        List<Point> points = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) {
            points.add(new Point(i % 3, 7));
        }

        assertThat(Dedupe.distinctPoints(points)).hasSize(3);
    }
}
