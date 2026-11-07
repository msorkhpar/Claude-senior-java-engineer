package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class QuorumWriterTest {

    private final ExecutorService replicaThreads = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void cleanUp() {
        replicaThreads.shutdownNow();
        Thread.interrupted();
    }

    /** Acknowledges inside store(), before the writer waits. */
    static QuorumWriter.Replica acksAtOnce() {
        return (version, record, ack) -> ack.run();
    }

    /** Acknowledges from a thread of its own, after store() has returned. */
    QuorumWriter.Replica acksFromItsOwnThread() {
        return (version, record, ack) -> replicaThreads.execute(ack);
    }

    /** Never acknowledges: the replica is down. */
    static QuorumWriter.Replica down() {
        return (version, record, ack) -> { };
    }

    @Test
    void writesSucceedWhenEveryReplicaAcknowledges() throws Exception {
        QuorumWriter writer = new QuorumWriter(
                List.of(acksAtOnce(), acksFromItsOwnThread(), acksFromItsOwnThread()), 2, 5_000);

        assertThat(writer.write("order-7731:paid")).isTrue();
        assertThat(writer.write("order-7731:shipped")).isTrue();
    }

    @Test
    void exactlyAQuorumOfAcknowledgementsIsEnough() throws Exception {
        QuorumWriter writer = new QuorumWriter(
                List.of(acksAtOnce(), acksFromItsOwnThread(), down(), down(), acksAtOnce()), 3, 1_000);

        Future<Boolean> written = replicaThreads.submit(() -> writer.write("ledger-204:credit"));
        assertThat(written.get(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void lateAcknowledgementsOfAnEarlierWriteDoNotCountForTheNextOne() throws Exception {
        List<Runnable> lateAcks = new CopyOnWriteArrayList<>();
        QuorumWriter.Replica alwaysUp = acksAtOnce();
        QuorumWriter.Replica upForTheFirstWriteOnly = (version, record, ack) -> {
            if (version == 1) ack.run();
        };
        QuorumWriter.Replica slowThenDown = (version, record, ack) -> {
            if (version == 1) lateAcks.add(ack);
        };
        QuorumWriter writer = new QuorumWriter(List.of(alwaysUp, upForTheFirstWriteOnly, slowThenDown), 2, 200);

        assertThat(writer.write("cart-55:add")).isTrue();
        Future<?> late = replicaThreads.submit(() -> lateAcks.forEach(Runnable::run));
        late.get(5, TimeUnit.SECONDS);

        Future<Boolean> second = replicaThreads.submit(() -> writer.write("cart-55:remove"));
        assertThat(second.get(5, TimeUnit.SECONDS)).isFalse();
    }

    @Test
    void aWriteWithoutAQuorumGivesUpAfterTheTimeout() throws Exception {
        QuorumWriter writer = new QuorumWriter(List.of(acksAtOnce(), down(), down()), 2, 100);

        Future<Boolean> written = replicaThreads.submit(() -> writer.write("invoice-88:void"));

        assertThat(written.get(5, TimeUnit.SECONDS)).isFalse();
    }
}
