package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeapSizingTest {

    private static final long KIB = 1024L;
    private static final long MIB = 1024L * KIB;
    private static final long GIB = 1024L * MIB;

    @Test
    void sizesTheHeapAndTheStack() {
        HeapSizing explicit = HeapSizing.parse(List.of("-Xmx4g", "-Xss512k", "-XX:+UseG1GC"), 8 * GIB);
        assertThat(explicit.maxHeapBytes()).isEqualTo(4 * GIB);
        assertThat(explicit.threadStackBytes()).isEqualTo(512 * KIB);

        HeapSizing container = HeapSizing.parse(List.of("-XX:MaxRAMPercentage=75.0"), 8 * GIB);
        assertThat(container.maxHeapBytes()).isEqualTo(6 * GIB);
        assertThat(container.threadStackBytes()).isEqualTo(MIB);

        HeapSizing defaults = HeapSizing.parse(List.of("-jar", "app.jar"), 8 * GIB);
        assertThat(defaults.maxHeapBytes()).isEqualTo(2 * GIB);

        HeapSizing odd = HeapSizing.parse(List.of("-XX:MaxRAMPercentage=50.0"), 3 * GIB + 1);
        assertThat(odd.maxHeapBytes()).isEqualTo(1536 * MIB);
    }

    @Test
    void sizeSuffixesCountIn1024s() {
        assertThat(HeapSizing.parse(List.of("-Xmx2G"), 8 * GIB).maxHeapBytes()).isEqualTo(2 * GIB);
        assertThat(HeapSizing.parse(List.of("-Xmx768M"), 8 * GIB).maxHeapBytes()).isEqualTo(768 * MIB);
        assertThat(HeapSizing.parse(List.of("-Xmx1048576k"), 8 * GIB).maxHeapBytes()).isEqualTo(GIB);
        assertThat(HeapSizing.parse(List.of("-Xmx5000000"), 8 * GIB).maxHeapBytes()).isEqualTo(5_000_000L);
        assertThat(HeapSizing.parse(List.of("-Xss2m"), 8 * GIB).threadStackBytes()).isEqualTo(2 * MIB);
    }

    @Test
    void anExplicitXmxBeatsThePercentage() {
        assertThat(HeapSizing.parse(List.of("-Xmx2g", "-XX:MaxRAMPercentage=75.0"), 8 * GIB).maxHeapBytes())
                .isEqualTo(2 * GIB);
        assertThat(HeapSizing.parse(List.of("-XX:MaxRAMPercentage=75.0", "-Xmx2g"), 8 * GIB).maxHeapBytes())
                .isEqualTo(2 * GIB);
    }

    @Test
    void theLastOccurrenceWins() {
        assertThat(HeapSizing.parse(List.of("-Xmx1g", "-Xmx3g"), 8 * GIB).maxHeapBytes()).isEqualTo(3 * GIB);
        assertThat(HeapSizing.parse(List.of("-Xss256k", "-Xss2m"), 8 * GIB).threadStackBytes()).isEqualTo(2 * MIB);
        assertThat(HeapSizing.parse(List.of("-XX:MaxRAMPercentage=75.0", "-XX:MaxRAMPercentage=50.0"), 8 * GIB)
                .maxHeapBytes()).isEqualTo(4 * GIB);
    }

    @Test
    void anInitialHeapAboveTheMaximumIsRefused() {
        assertThatThrownBy(() -> HeapSizing.parse(List.of("-Xms4g", "-Xmx2g"), 8 * GIB))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HeapSizing.parse(List.of("-Xmx2g", "-Xms3g"), 8 * GIB))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(HeapSizing.parse(List.of("-Xms2g", "-Xmx2g"), 8 * GIB).maxHeapBytes()).isEqualTo(2 * GIB);
    }
}
