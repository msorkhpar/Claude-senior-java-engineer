package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class PartitionWritersTest {

    /** Writers that record what happens to them; those named in failing throw when closed. */
    static final class Store implements PartitionWriters.Opener {
        final List<String> events = new ArrayList<>();
        final Set<String> failing;

        Store(String... failing) {
            this.failing = Set.of(failing);
        }

        @Override
        public PartitionWriters.Writer open(String partition) {
            events.add("open " + partition);
            return new PartitionWriters.Writer() {
                @Override
                public void write(String row) {
                    events.add("write " + partition + " " + row);
                }

                @Override
                public void close() throws IOException {
                    events.add("close " + partition);
                    if (failing.contains(partition)) throw new IOException("flush of " + partition + " failed");
                }
            };
        }

        List<String> closed() {
            return events.stream().filter(e -> e.startsWith("close ")).map(e -> e.substring(6)).toList();
        }
    }

    private static PartitionWriters runWith(Store store) throws Exception {
        PartitionWriters run = new PartitionWriters(store);
        run.write("eu-west/2026-03-09", "sensor-17,8.25");
        run.write("us-east/2026-03-09", "sensor-4,3.5");
        run.write("eu-west/2026-03-09", "sensor-18,7.0");
        run.write("ap-south/2026-03-10", "sensor-90,1.75");
        return run;
    }

    @Test
    void closingTheRunClosesEveryWriterItOpened() throws Exception {
        Store store = new Store();
        PartitionWriters run = runWith(store);

        run.close();

        assertThat(store.closed()).containsExactlyInAnyOrder(
                "eu-west/2026-03-09", "us-east/2026-03-09", "ap-south/2026-03-10");
    }

    @Test
    void aWriterThatFailsToCloseLeavesNoOtherOpen() throws Exception {
        Store store = new Store("us-east/2026-03-09");
        PartitionWriters run = runWith(store);

        Throwable thrown = catchThrowable(run::close);

        assertThat(thrown).isInstanceOf(IOException.class);
        assertThat(store.closed()).containsExactlyInAnyOrder(
                "eu-west/2026-03-09", "us-east/2026-03-09", "ap-south/2026-03-10");
    }

    @Test
    void closesTheNewestWriterFirst() throws Exception {
        Store store = new Store();
        PartitionWriters run = runWith(store);

        run.close();

        assertThat(store.closed()).containsExactly(
                "ap-south/2026-03-10", "us-east/2026-03-09", "eu-west/2026-03-09");
    }

    @Test
    void theFirstFailureIsTheOneThrown() throws Exception {
        Store store = new Store("eu-west/2026-03-09", "ap-south/2026-03-10");
        PartitionWriters run = runWith(store);

        Throwable thrown = catchThrowable(run::close);

        assertThat(thrown).hasMessage("flush of ap-south/2026-03-10 failed");
    }

    @Test
    void theOtherFailuresTravelWithIt() throws Exception {
        Store store = new Store("eu-west/2026-03-09", "us-east/2026-03-09", "ap-south/2026-03-10");
        PartitionWriters run = runWith(store);

        Throwable thrown = catchThrowable(run::close);

        assertThat(thrown).isNotNull();
        assertThat(thrown.getSuppressed()).extracting(Throwable::getMessage).containsExactlyInAnyOrder(
                "flush of us-east/2026-03-09 failed", "flush of eu-west/2026-03-09 failed");
    }
}
