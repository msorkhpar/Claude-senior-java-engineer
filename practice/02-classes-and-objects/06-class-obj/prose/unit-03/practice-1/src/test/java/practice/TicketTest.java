package practice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class TicketTest {

    @Test
    void countsEveryTicketAndNumbersThemInOrder() {
        int before = Ticket.issued();
        Ticket first = new Ticket();
        int firstNumber = first.number();
        Ticket second = new Ticket();
        int secondNumber = second.number();
        assertThat(firstNumber).isEqualTo(before + 1);
        assertThat(secondNumber).isEqualTo(firstNumber + 1);
        assertThat(Ticket.issued()).isEqualTo(before + 2);
    }

    @Test
    void eachTicketKeepsItsOwnNumber() {
        Ticket first = new Ticket();
        int firstNumber = first.number();
        new Ticket();
        new Ticket();
        assertThat(first.number()).isEqualTo(firstNumber);
    }

    /**
     * No race decides this test. While the test holds the class's lock, a thread
     * creating a ticket must be parked waiting for it; an unguarded count finishes
     * at once and is caught every time.
     */
    @Test
    void ticketsIssuedAtOnceAreAllCounted() {
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            new Ticket();
            int before = Ticket.issued();
            Thread creator = new Thread(Ticket::new, "creator");
            boolean waited;
            synchronized (Ticket.class) {
                creator.start();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (System.nanoTime() < deadline
                        && creator.isAlive()
                        && creator.getState() != Thread.State.BLOCKED) {
                    Thread.sleep(10);
                }
                waited = creator.isAlive() && creator.getState() == Thread.State.BLOCKED;
            }
            creator.join(10_000);
            assertThat(waited).as("creating a ticket waited for the class's lock").isTrue();
            assertThat(Ticket.issued()).isEqualTo(before + 1);

            int threads = 4;
            int perThread = 2_000;
            int start = Ticket.issued();
            Set<Integer> numbers = ConcurrentHashMap.newKeySet();
            List<Thread> workers = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                Thread worker = new Thread(() -> {
                    for (int i = 0; i < perThread; i++) {
                        numbers.add(new Ticket().number());
                    }
                });
                workers.add(worker);
                worker.start();
            }
            for (Thread worker : workers) {
                worker.join(20_000);
            }
            assertThat(Ticket.issued()).isEqualTo(start + threads * perThread);
            assertThat(numbers).hasSize(threads * perThread);
        });
    }
}
