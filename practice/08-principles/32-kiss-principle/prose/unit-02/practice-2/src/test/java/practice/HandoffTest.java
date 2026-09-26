package practice;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class HandoffTest {

    /** Spins until the thread is parked (WAITING) or finished; neither outcome depends on time. */
    private static void awaitParkedOrDone(Thread thread) {
        while (thread.getState() != Thread.State.WAITING && thread.getState() != Thread.State.TERMINATED) {
            Thread.onSpinWait();
        }
    }

    @Test
    void passesItemsInOrder() throws Exception {
        Handoff<String> handoff = new Handoff<>(10);
        for (int i = 1; i <= 3; i++) {
            handoff.produce("item" + i);
        }
        assertThat(handoff.size()).isEqualTo(3);
        assertThat(handoff.consume()).isEqualTo("item1");
        assertThat(handoff.consume()).isEqualTo("item2");
        assertThat(handoff.consume()).isEqualTo("item3");
        assertThat(handoff.size()).isZero();
    }

    @Test
    void consumeWaitsForAnItem() throws Exception {
        Handoff<String> handoff = new Handoff<>(10);
        AtomicReference<String> received = new AtomicReference<>();
        Thread consumer = new Thread(() -> {
            try {
                received.set(handoff.consume());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();
        awaitParkedOrDone(consumer);
        handoff.produce("late item");
        consumer.join();
        assertThat(received.get()).isEqualTo("late item");
    }

    @Test
    void aFullHandoffMakesTheProducerWait() throws Exception {
        Handoff<String> handoff = new Handoff<>(1);
        handoff.produce("a");
        Thread producer = new Thread(() -> {
            try {
                handoff.produce("b");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();
        awaitParkedOrDone(producer);
        assertThat(producer.isAlive()).as("the producer waits while the hand-off is full").isTrue();
        assertThat(handoff.size()).isEqualTo(1);
        assertThat(handoff.consume()).isEqualTo("a");
        producer.join();
        assertThat(handoff.consume()).isEqualTo("b");
    }

    @Test
    void anInterruptedWaitThrows() throws Exception {
        Handoff<String> handoff = new Handoff<>(1);
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread consumer = new Thread(() -> {
            try {
                outcome.set(handoff.consume());
            } catch (InterruptedException e) {
                outcome.set(e);
            }
        });
        consumer.start();
        awaitParkedOrDone(consumer);
        consumer.interrupt();
        consumer.join();
        assertThat(outcome.get()).as("an interrupted consume() throws").isInstanceOf(InterruptedException.class);
        handoff.produce("a");
        outcome.set(null);
        Thread producer = new Thread(() -> {
            try {
                handoff.produce("b");
                outcome.set("produce returned");
            } catch (InterruptedException e) {
                outcome.set(e);
            }
        });
        producer.start();
        awaitParkedOrDone(producer);
        producer.interrupt();
        producer.join();
        assertThat(outcome.get()).as("an interrupted produce() throws").isInstanceOf(InterruptedException.class);
        assertThat(handoff.size()).isEqualTo(1);
    }

    @Test
    void nextRestoresTheInterruptFlag() throws Exception {
        Handoff<String> handoff = new Handoff<>(2);
        Thread.currentThread().interrupt();
        Throwable thrown;
        boolean restored;
        try {
            thrown = catchThrowable(handoff::next);
        } finally {
            restored = Thread.interrupted();
        }
        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(InterruptedException.class);
        assertThat(restored).as("the interrupt flag is set again after next()").isTrue();
    }
}
