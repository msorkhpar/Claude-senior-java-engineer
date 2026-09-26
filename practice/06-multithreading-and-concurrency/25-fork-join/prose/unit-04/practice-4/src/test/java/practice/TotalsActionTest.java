package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
/** The pool has ONE worker, so a child's action runs only when its parent waits for it. */
class TotalsActionTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(1);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static TotalsAction.Node node(long size, TotalsAction.Node... children) {
        return new TotalsAction.Node(size, List.of(children));
    }

    @Test
    void totalsATwoLevelTree() {
        TotalsAction.Node a = node(2);
        TotalsAction.Node b = node(3);
        TotalsAction.Node root = node(1, a, b);
        pool.invoke(new TotalsAction(root));
        assertThat(root.total()).isEqualTo(6);
        assertThat(a.total()).isEqualTo(2);
        assertThat(b.total()).isEqualTo(3);
    }

    @Test
    void aLeafNodeTotalsItsOwnSize() {
        TotalsAction.Node solo = node(5);
        pool.invoke(new TotalsAction(solo));
        assertThat(solo.total()).isEqualTo(5);
    }

    @Test
    void aNodeWithThreeChildren() {
        TotalsAction.Node root = node(1, node(2), node(3), node(4));
        pool.invoke(new TotalsAction(root));
        assertThat(root.total()).isEqualTo(10);
    }

    @Test
    void totalsReachDownADeepTree() {
        TotalsAction.Node z = node(4);
        TotalsAction.Node y = node(3, z);
        TotalsAction.Node x = node(2, y);
        TotalsAction.Node root = node(1, x);
        pool.invoke(new TotalsAction(root));
        assertThat(List.of(root.total(), x.total(), y.total(), z.total())).containsExactly(10L, 9L, 7L, 4L);
    }
}
