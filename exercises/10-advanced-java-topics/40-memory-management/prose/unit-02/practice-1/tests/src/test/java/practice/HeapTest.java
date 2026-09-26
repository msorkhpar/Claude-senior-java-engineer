package practice;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class HeapTest {

    @Test
    void freesWhatNoRootReaches() {
        Heap heap = new Heap();
        heap.allocate("A");
        heap.allocate("B");
        heap.allocate("C");
        heap.allocate("D");
        heap.addRoot("A");
        heap.reference("A", "B");
        heap.reference("B", "D");

        assertThat(heap.collect()).containsExactly("C");
        assertThat(heap.live()).containsExactly("A", "B", "D");
        assertThatThrownBy(() -> heap.allocate("A")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCycleNoRootReachesIsFreed() {
        Heap heap = new Heap();
        heap.allocate("R");
        heap.allocate("X");
        heap.allocate("Y");
        heap.addRoot("R");
        heap.reference("X", "Y");
        heap.reference("Y", "X");

        assertThat(heap.collect()).containsExactly("X", "Y");
        assertThat(heap.live()).containsExactly("R");
    }

    @Test
    void aCycleAmongLiveObjectsIsMarkedOnce() {
        Heap heap = new Heap();
        heap.allocate("A");
        heap.allocate("B");
        heap.addRoot("A");
        heap.reference("A", "B");
        heap.reference("B", "A");

        assertThat(heap.collect()).isEmpty();
        assertThat(heap.live()).containsExactly("A", "B");
    }

    @Test
    void marksAreClearedForTheNextCollection() {
        Heap heap = new Heap();
        heap.allocate("A");
        heap.allocate("B");
        heap.addRoot("A");
        heap.reference("A", "B");
        assertThat(heap.collect()).isEmpty();

        heap.removeRoot("A");

        assertThat(heap.collect()).containsExactly("A", "B");
        assertThat(heap.live()).isEmpty();
    }
}
