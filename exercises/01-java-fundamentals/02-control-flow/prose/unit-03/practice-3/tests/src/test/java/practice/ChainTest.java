package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChainTest {

    private static Chain.Node list(int... values) {
        Chain.Node head = null;
        for (int i = values.length - 1; i >= 0; i--) {
            head = new Chain.Node(values[i], head);
        }
        return head;
    }

    @Test
    void findsTheLink() {
        Chain.Node head = list(1, 3, 5, 7);
        assertThat(Chain.indexOf(head, 1)).isZero();
        assertThat(Chain.indexOf(head, 5)).isEqualTo(2);
        assertThat(Chain.indexOf(head, 7)).isEqualTo(3);
    }

    @Test
    void theFirstLinkWins() {
        assertThat(Chain.indexOf(list(1, 3, 5, 3), 3)).isEqualTo(1);
    }

    @Test
    void aMissingValueIsMinusOne() {
        assertThat(Chain.indexOf(list(1, 3, 5), 4)).isEqualTo(-1);
    }

    @Test
    void anEmptyListHasNoLinks() {
        assertThat(Chain.indexOf(null, 1)).isEqualTo(-1);
    }
}
