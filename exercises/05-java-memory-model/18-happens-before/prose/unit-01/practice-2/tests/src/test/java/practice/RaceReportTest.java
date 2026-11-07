package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.RaceReport.Kind.*;

class RaceReportTest {

    private static RaceReport.Action a(String thread, RaceReport.Kind kind, String target) {
        return new RaceReport.Action(thread, kind, target);
    }

    private static List<String> races(RaceReport.Action... trace) {
        return new RaceReport(List.of(trace)).races();
    }

    @Test
    void aCounterUpdatedUnderOneLockHasNoRaces() {
        assertThat(races(
                a("api", LOCK, "quotaLock"),
                a("api", READ, "remaining"),
                a("api", WRITE, "remaining"),
                a("api", UNLOCK, "quotaLock"),
                a("billing", LOCK, "quotaLock"),
                a("billing", READ, "remaining"),
                a("billing", WRITE, "remaining"),
                a("billing", UNLOCK, "quotaLock"))).isEmpty();
    }

    @Test
    void aWorkerGivenItsInputBeforeStartAndJoinedAfterwardsHasNoRaces() {
        assertThat(races(
                a("main", WRITE, "region"),
                a("main", START, "exporter"),
                a("exporter", READ, "region"),
                a("exporter", WRITE, "rowsWritten"),
                a("main", JOIN, "exporter"),
                a("main", READ, "rowsWritten"))).isEmpty();
    }

    @Test
    void aHandoffThroughOneVolatileFieldHasNoRaces() {
        assertThat(races(
                a("producer", WRITE, "order"),
                a("producer", VOLATILE_WRITE, "published"),
                a("consumer", VOLATILE_READ, "published"),
                a("consumer", READ, "order"))).isEmpty();
    }

    @Test
    void aWriteEarlierInTheRunIsNotOrderedBeforeAnotherThreadsRead() {
        assertThat(races(
                a("indexer", WRITE, "lastIndexed"),
                a("indexer", VOLATILE_WRITE, "heartbeat"),
                a("web", READ, "lastIndexed"))).containsExactly("0-2");
    }

    @Test
    void locksOnTwoDifferentMonitorsDoNotOrderTheirThreads() {
        assertThat(races(
                a("cart", LOCK, "cartLock"),
                a("cart", WRITE, "itemCount"),
                a("cart", UNLOCK, "cartLock"),
                a("checkout", LOCK, "orderLock"),
                a("checkout", READ, "itemCount"),
                a("checkout", UNLOCK, "orderLock"))).containsExactly("1-4");
    }

    @Test
    void aVolatileWriteDoesNotOrderAReadOfAnotherVolatileField() {
        assertThat(races(
                a("sensor", WRITE, "reading"),
                a("sensor", VOLATILE_WRITE, "sampleSeq"),
                a("display", VOLATILE_READ, "alarmSeq"),
                a("display", READ, "reading"))).containsExactly("0-3");
    }

    @Test
    void aWriteMadeAfterStartingAThreadIsNotOrderedBeforeThatThreadsReads() {
        assertThat(races(
                a("main", WRITE, "batchSize"),
                a("main", START, "uploader"),
                a("main", WRITE, "retries"),
                a("uploader", READ, "batchSize"),
                a("uploader", READ, "retries"))).containsExactly("2-4");
    }

    @Test
    void aWriteCarriedThroughALockAndThenAStartIsOrdered() {
        assertThat(races(
                a("ingest", WRITE, "batch"),
                a("ingest", LOCK, "queueLock"),
                a("ingest", UNLOCK, "queueLock"),
                a("router", LOCK, "queueLock"),
                a("router", UNLOCK, "queueLock"),
                a("router", START, "sender"),
                a("sender", READ, "batch"),
                a("ingest", WRITE, "batch"))).containsExactly("6-7");
    }
}
