package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphTest {

    private static Graph<String> pageMap() {
        Graph<String> g = new Graph<>(false);
        g.addEdge("New York", "Boston");
        g.addEdge("New York", "Philadelphia");
        g.addEdge("Boston", "Portland");
        g.addEdge("Philadelphia", "Washington");
        return g;
    }

    @Test
    void searchesThePageMap() {
        Graph<String> g = pageMap();

        assertThat(g.bfs("New York"))
                .containsExactly("New York", "Boston", "Philadelphia", "Portland", "Washington");
        assertThat(g.shortestPath("Portland", "Washington"))
                .containsExactly("Portland", "Boston", "New York", "Philadelphia", "Washington");
    }

    @Test
    void eachVertexIsVisitedOnce() {
        Graph<String> g = new Graph<>(false);
        g.addEdge("A", "B");
        g.addEdge("A", "C");
        g.addEdge("B", "C");
        g.addEdge("C", "D");

        assertThat(g.bfs("A")).containsExactly("A", "B", "C", "D");
    }

    @Test
    void aDirectedEdgeGoesOneWay() {
        Graph<String> g = new Graph<>(true);
        g.addEdge("A", "B");
        g.addEdge("B", "C");

        assertThat(g.bfs("B")).containsExactly("B", "C");
        assertThat(g.shortestPath("C", "A")).isEmpty();
        assertThat(g.shortestPath("A", "C")).containsExactly("A", "B", "C");
    }

    @Test
    void noPathGivesAnEmptyList() {
        Graph<String> g = new Graph<>(false);
        g.addEdge("A", "B");
        g.addEdge("C", "D");

        assertThat(g.shortestPath("A", "D")).isNotNull().isEmpty();
        assertThat(g.shortestPath("A", "Z")).isNotNull().isEmpty();
        assertThat(g.shortestPath("Z", "A")).isNotNull().isEmpty();
        assertThat(g.bfs("Z")).isNotNull().isEmpty();
    }

    @Test
    void aVertexReachesItselfAlone() {
        Graph<String> g = pageMap();

        assertThat(g.shortestPath("Boston", "Boston")).containsExactly("Boston");
    }
}
