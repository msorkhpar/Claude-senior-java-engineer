package practice;

import java.lang.management.MemoryUsage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MemoryReportTest {

    private static final long MIB = 1024L * 1024;

    @Test
    void reportsAPoolInWholeMebibytes() {
        MemoryUsage oldGen = new MemoryUsage(64 * MIB, 100 * MIB, 512 * MIB, 512 * MIB);

        assertThat(MemoryReport.line("G1 Old Gen", oldGen)).isEqualTo("Pool [G1 Old Gen]: used=100MB, max=512MB");
        assertThat(MemoryReport.usedPercent(oldGen)).isPresent();
        assertThat(MemoryReport.usedPercent(oldGen).getAsDouble()).isCloseTo(19.53125, within(1e-9));
    }

    @Test
    void mebibytesRoundDown() {
        MemoryUsage eden = new MemoryUsage(0, 1_572_863L, 2 * MIB, 4 * MIB);

        assertThat(MemoryReport.line("G1 Eden Space", eden)).isEqualTo("Pool [G1 Eden Space]: used=1MB, max=4MB");
        assertThat(MemoryReport.line("tiny", new MemoryUsage(0, 1_000_000L, 1_000_000L, 1_500_000L)))
                .isEqualTo("Pool [tiny]: used=0MB, max=1MB");
    }

    @Test
    void anUndefinedMaximumPrintsMinusOne() {
        MemoryUsage metaspace = new MemoryUsage(0, 30 * MIB, 32 * MIB, -1);

        assertThat(MemoryReport.line("Metaspace", metaspace)).isEqualTo("Pool [Metaspace]: used=30MB, max=-1MB");
        assertThat(MemoryReport.usedPercent(metaspace)).isEmpty();
    }

    @Test
    void usageIsMeasuredAgainstTheMaximum() {
        MemoryUsage eden = new MemoryUsage(0, 1_572_863L, 2 * MIB, 4 * MIB);

        assertThat(MemoryReport.usedPercent(eden)).isPresent();
        assertThat(MemoryReport.usedPercent(eden).getAsDouble()).isCloseTo(37.49997615814209, within(1e-9));
    }
}
