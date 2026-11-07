package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxTest {

    private static Outbox.Message msg(String id, long deadline) {
        return new Outbox.Message(id, deadline);
    }

    private static Outbox outbox(List<Outbox.Message> list, Outbox.Message... messages) {
        Outbox outbox = new Outbox(list);
        for (Outbox.Message m : messages) outbox.add(m);
        return outbox;
    }

    @Test
    void nothingIsDroppedBeforeAnyDeadline() {
        Outbox outbox = outbox(new ArrayList<>(), msg("inv-11", 900), msg("inv-12", 950), msg("inv-13", 990));

        assertThat(outbox.dropExpired(899)).isZero();
        assertThat(outbox.pendingIds()).containsExactly("inv-11", "inv-12", "inv-13");
    }

    @Test
    void aMessageWhoseDeadlineIsNowIsDropped() {
        Outbox outbox = outbox(new ArrayList<>(),
                msg("po-21", 2_000), msg("po-22", 2_000), msg("po-23", 1_500), msg("po-24", 2_000));

        assertThat(outbox.dropExpired(1_500)).isEqualTo(1);
        assertThat(outbox.pendingIds()).containsExactly("po-21", "po-22", "po-24");
    }

    @Test
    void expiredMessagesApartFromEachOtherAreAllDropped() {
        Outbox outbox = outbox(new ArrayList<>(),
                msg("ack-31", 100), msg("ack-32", 700), msg("ack-33", 120), msg("ack-34", 800), msg("ack-35", 650));

        assertThat(outbox.dropExpired(300)).isEqualTo(2);
        assertThat(outbox.pendingIds()).containsExactly("ack-32", "ack-34", "ack-35");
    }

    @Test
    void neighbouringExpiredMessagesAreAllDropped() {
        Outbox outbox = outbox(new ArrayList<>(),
                msg("rcpt-41", 5_000), msg("rcpt-42", 40), msg("rcpt-43", 45), msg("rcpt-44", 5_000));

        assertThat(outbox.dropExpired(60)).isEqualTo(2);
        assertThat(outbox.pendingIds()).containsExactly("rcpt-41", "rcpt-44");
    }

    @Test
    void theLiveOutboxDropsItsExpiredMessagesToo() {
        Outbox outbox = outbox(new CopyOnWriteArrayList<>(),
                msg("evt-51", 70), msg("evt-52", 310), msg("evt-53", 20));

        assertThat(outbox.dropExpired(100)).isEqualTo(2);
        assertThat(outbox.pendingIds()).containsExactly("evt-52");
    }
}
