package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SplitTreeTest {

    @Test
    void aBalancedSplit() {
        assertThat(SplitTree.shape(10_000, 1_000, 50)).isEqualTo(new SplitTree.Shape(4, 16, 31));
        assertThat(SplitTree.shape(1_000, 100, 50)).isEqualTo(new SplitTree.Shape(4, 16, 31));
    }

    @Test
    void aRangeOfThresholdLengthIsOneLeaf() {
        assertThat(SplitTree.shape(1_000, 1_000, 50)).isEqualTo(new SplitTree.Shape(0, 1, 1));
    }

    @Test
    void anUnbalancedSplitGoesDeeper() {
        assertThat(SplitTree.shape(1_000, 100, 10)).isEqualTo(new SplitTree.Shape(23, 24, 47));
    }

    @Test
    void aSplitAlwaysMakesProgress() {
        assertThat(SplitTree.shape(20, 1, 10)).isEqualTo(new SplitTree.Shape(18, 20, 39));
    }

    @Test
    void oddLengthsAreWalkedNotEstimated() {
        assertThat(SplitTree.shape(7, 3, 50)).isEqualTo(new SplitTree.Shape(2, 3, 5));
    }
}
