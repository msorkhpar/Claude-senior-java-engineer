package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTreeTest {

    private static SearchTree<Integer> tree(int... values) {
        SearchTree<Integer> tree = new SearchTree<>();
        for (int v : values) {
            tree.insert(v);
        }
        return tree;
    }

    @Test
    void walksThePageTreeInAllFourOrders() {
        SearchTree<Integer> tree = tree(5, 3, 7, 1, 4);

        assertThat(tree.inOrder()).containsExactly(1, 3, 4, 5, 7);
        assertThat(tree.preOrder()).containsExactly(5, 3, 1, 4, 7);
        assertThat(tree.postOrder()).containsExactly(1, 4, 3, 7, 5);
        assertThat(tree.levelOrder()).containsExactly(5, 3, 7, 1, 4);
        assertThat(tree.height()).isEqualTo(2);
    }

    @Test
    void duplicatesAreIgnored() {
        SearchTree<Integer> tree = tree(5, 3, 5, 3, 7);

        assertThat(tree.inOrder()).containsExactly(3, 5, 7);
        assertThat(tree.height()).isEqualTo(1);
    }

    @Test
    void levelOrderFinishesALevelBeforeTheNext() {
        SearchTree<Integer> tree = tree(8, 4, 12, 2, 6, 10, 14, 1, 3);

        assertThat(tree.levelOrder()).containsExactly(8, 4, 12, 2, 6, 10, 14, 1, 3);
    }

    @Test
    void anEmptyTreeHasNothingAndHeightMinusOne() {
        SearchTree<Integer> empty = new SearchTree<>();

        assertThat(empty.inOrder()).isEmpty();
        assertThat(empty.preOrder()).isEmpty();
        assertThat(empty.postOrder()).isEmpty();
        assertThat(empty.levelOrder()).isEmpty();
        assertThat(empty.height()).isEqualTo(-1);
        assertThat(tree(9).height()).isZero();
        assertThat(tree(9).levelOrder()).isEqualTo(List.of(9));
    }
}
