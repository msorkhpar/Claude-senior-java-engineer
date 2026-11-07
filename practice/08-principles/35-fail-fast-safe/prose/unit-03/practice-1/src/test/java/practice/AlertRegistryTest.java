package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import practice.AlertRegistry.Tick;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class AlertRegistryTest {

    private final ExecutorService feed = daemonPool("feed");
    private final ExecutorService ui = daemonPool("ui");
    private final CountDownLatch release = new CountDownLatch(1);

    private static ExecutorService daemonPool(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    @AfterEach
    void cleanUp() {
        release.countDown();
        feed.shutdownNow();
        ui.shutdownNow();
    }

    /** An alert that records what it got under a name. */
    private static Consumer<Tick> recording(String name, List<String> log) {
        return tick -> log.add(name + ":" + tick.symbol() + "@" + tick.priceCents());
    }

    /** An alert that signals when a tick reaches it, then holds the feed until released. */
    private Consumer<Tick> slow(CountDownLatch reached, List<String> log) {
        return tick -> {
            log.add("slow:" + tick.symbol() + "@" + tick.priceCents());
            reached.countDown();
            try {
                if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("never released");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    @Test
    void everySubscribedAlertGetsEachTickOnce() throws Exception {
        AlertRegistry registry = new AlertRegistry();
        List<String> log = new CopyOnWriteArrayList<>();
        Consumer<Tick> bond = recording("bond", log), fx = recording("fx", log), oil = recording("oil", log);
        registry.subscribe(bond);
        registry.subscribe(fx);
        registry.subscribe(oil);
        registry.unsubscribe(fx);

        int reached = feed.submit(() -> registry.publish(new Tick("NOVA", 18_250))).get(5, TimeUnit.SECONDS);

        assertThat(reached).isEqualTo(2);
        assertThat(log).containsExactlyInAnyOrder("bond:NOVA@18250", "oil:NOVA@18250");
    }

    @Test
    void aPublishSurvivesASubscribeFromAnotherThreadWhileItRuns() throws Exception {
        AlertRegistry registry = new AlertRegistry();
        List<String> log = new CopyOnWriteArrayList<>();
        CountDownLatch reached = new CountDownLatch(1);
        registry.subscribe(slow(reached, log));
        registry.subscribe(recording("gold", log));
        registry.subscribe(recording("rates", log));

        Future<Integer> publish = feed.submit(() -> registry.publish(new Tick("KALE", 731)));
        assertThat(reached.await(5, TimeUnit.SECONDS)).as("the tick reached the first alert").isTrue();
        Future<?> subscribe = ui.submit(() -> registry.subscribe(recording("late", log)));
        try {
            subscribe.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // another case checks that a subscribe doesn't wait; here only the publish matters
        }
        release.countDown();

        try {
            publish.get(5, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            fail("the publish failed when a user subscribed while it ran: " + e.getCause());
        }
        assertThat(log).contains("gold:KALE@731", "rates:KALE@731");
    }

    @Test
    void aSubscribeDoesNotWaitForASlowAlert() throws Exception {
        AlertRegistry registry = new AlertRegistry();
        List<String> log = new CopyOnWriteArrayList<>();
        CountDownLatch reached = new CountDownLatch(1);
        registry.subscribe(slow(reached, log));
        registry.subscribe(recording("gold", log));

        feed.submit(() -> registry.publish(new Tick("KALE", 731)));
        assertThat(reached.await(5, TimeUnit.SECONDS)).as("the tick reached the first alert").isTrue();
        Future<?> subscribe = ui.submit(() -> registry.subscribe(recording("late", log)));
        try {
            subscribe.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("subscribe() was still waiting while one alert was slow to handle a tick");
        }
    }
}
