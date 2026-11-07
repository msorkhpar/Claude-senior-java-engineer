package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveReaderTest {

    private static final String ARCHIVE = "ARC1\nmeter-3301,2026-02-01,118.4\nmeter-3301,2026-02-02,121.0\n";

    @Test
    void readsTheFirstRecordOfEachArchiveWithoutRunningOutOfConnections() throws IOException {
        ArchiveReader.Pool pool = new ArchiveReader.Pool(1, ARCHIVE);

        for (int i = 0; i < 3; i++) {
            assertThat(ArchiveReader.firstRecord(pool)).isEqualTo("meter-3301,2026-02-01,118.4");
        }
    }

    @Test
    void aFileThatIsNotAnArchiveStillGivesItsConnectionBack() {
        ArchiveReader.Pool pool = new ArchiveReader.Pool(1, "meter,day,kwh\nmeter-3301,2026-02-01,118.4\n");

        assertThatThrownBy(() -> ArchiveReader.firstRecord(pool)).isInstanceOf(IOException.class);

        assertThat(pool.idle()).hasSize(1);
    }

    @Test
    void aConnectionGoesBackToThePoolOnlyOnce() throws IOException {
        ArchiveReader.Pool pool = new ArchiveReader.Pool(2, ARCHIVE);

        ArchiveReader.firstRecord(pool);

        ArchiveReader.Lease first = pool.borrow();
        ArchiveReader.Lease second = pool.borrow();
        assertThat(first).isNotSameAs(second);
    }
}
