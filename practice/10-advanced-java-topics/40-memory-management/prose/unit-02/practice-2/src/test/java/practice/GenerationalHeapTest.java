package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import practice.GenerationalHeap.Generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerationalHeapTest {

    @Test
    void promotesThePageObjectAfterItsThirdSurvivorAge() {
        GenerationalHeap heap = new GenerationalHeap(3);
        heap.allocate("obj");
        heap.allocate("temp");
        assertThat(heap.generation("obj")).isEqualTo(Generation.EDEN);
        assertThat(heap.age("obj")).isZero();

        assertThat(heap.minorGc(Set.of("obj"))).containsExactly("temp");
        List<Generation> seen = new ArrayList<>();
        seen.add(heap.generation("obj"));
        for (int i = 0; i < 4; i++) {
            assertThat(heap.minorGc(Set.of("obj"))).isEmpty();
            seen.add(heap.generation("obj"));
        }

        assertThat(seen).containsExactly(Generation.SURVIVOR, Generation.SURVIVOR, Generation.SURVIVOR,
                Generation.OLD, Generation.OLD);
        assertThat(heap.age("obj")).isEqualTo(4);
        assertThatThrownBy(() -> heap.generation("temp")).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void aMinorGcLeavesTheOldGenerationAlone() {
        GenerationalHeap heap = new GenerationalHeap(1);
        heap.allocate("cache");
        heap.minorGc(Set.of("cache"));
        heap.minorGc(Set.of("cache"));
        assertThat(heap.generation("cache")).isEqualTo(Generation.OLD);

        assertThat(heap.minorGc(Set.of())).isEmpty();
        assertThat(heap.generation("cache")).isEqualTo(Generation.OLD);
        assertThat(heap.age("cache")).isEqualTo(2);

        assertThat(heap.majorGc(Set.of())).containsExactly("cache");
    }

    @Test
    void theThresholdIsTheCallersSetting() {
        GenerationalHeap heap = new GenerationalHeap(1);
        heap.allocate("obj");

        heap.minorGc(Set.of("obj"));
        assertThat(heap.generation("obj")).isEqualTo(Generation.SURVIVOR);
        heap.minorGc(Set.of("obj"));
        assertThat(heap.generation("obj")).isEqualTo(Generation.OLD);
    }

    @Test
    void aMajorGcCollectsEveryGeneration() {
        GenerationalHeap heap = new GenerationalHeap(1);
        heap.allocate("old");
        heap.minorGc(Set.of("old"));
        heap.minorGc(Set.of("old"));
        heap.allocate("survivor");
        heap.minorGc(Set.of("old", "survivor"));
        heap.allocate("eden");
        heap.allocate("kept");

        assertThat(heap.majorGc(Set.of("kept"))).containsExactly("old", "survivor", "eden");
        assertThat(heap.generation("kept")).isEqualTo(Generation.EDEN);
    }
}
