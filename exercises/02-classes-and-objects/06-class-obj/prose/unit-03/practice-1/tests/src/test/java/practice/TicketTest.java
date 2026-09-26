package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void ticketsIssuedAtOnceAreAllCounted() throws Exception {
        int threads = 8;
        int perThread = 20_000;
        int before = Ticket.issued();
        Set<Integer> numbers = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> done = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                done.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        numbers.add(new Ticket().number());
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : done) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        assertThat(Ticket.issued()).isEqualTo(before + threads * perThread);
        assertThat(numbers).hasSize(threads * perThread);
    }
}
