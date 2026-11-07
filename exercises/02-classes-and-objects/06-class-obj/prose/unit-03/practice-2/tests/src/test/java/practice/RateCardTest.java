package practice;

import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each test loads a fresh copy of RateCard through a class loader of its own, so every test starts
 * with a class that has not been initialized, as after a restart.
 */
class RateCardTest {

    private static final Map<String, Long> RATES = Map.of("CHF", 94L, "GBP", 84L, "SEK", 1132L);

    /** A fresh, uninitialized RateCard whose Source answers with {@code source}. */
    private static Class<?> freshRateCard(Supplier<Map<String, Long>> source) throws Exception {
        URL classes = RateCard.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader loader = new URLClassLoader(new URL[] {classes}, ClassLoader.getPlatformClassLoader());
        useSource(loader, source);
        return Class.forName("practice.RateCard", false, loader);
    }

    private static void useSource(ClassLoader loader, Supplier<Map<String, Long>> source) throws Exception {
        Class.forName("practice.RateCard$Source", true, loader).getField("rates").set(null, source);
    }

    /** Calls a public static method of the fresh copy on a daemon thread of its own. */
    private static FutureTask<Object> started(Class<?> card, String name, Object... args) {
        Callable<Object> call = () -> {
            for (Method m : card.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                    return m.invoke(null, args);
                }
            }
            throw new NoSuchMethodException(name);
        };
        FutureTask<Object> task = new FutureTask<>(call);
        Thread t = new Thread(task, "request");
        t.setDaemon(true);
        t.start();
        return task;
    }

    /** What the call returned, or what it threw. */
    private static Object outcome(FutureTask<Object> task) throws Exception {
        try {
            return task.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable thrown = e.getCause();
            return thrown instanceof InvocationTargetException ite ? ite.getCause() : thrown;
        }
    }

    private static Object call(Class<?> card, String name, Object... args) throws Exception {
        return outcome(started(card, name, args));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void pricesUseTheRatesFromTheRateService() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        Class<?> card = freshRateCard(() -> {
            loads.incrementAndGet();
            return RATES;
        });

        assertThat(call(card, "centsPerEuro", "CHF")).isEqualTo(94L);
        assertThat(call(card, "centsPerEuro", "SEK")).isEqualTo(1132L);
        assertThat(call(card, "centsPerEuro", "GBP")).isEqualTo(84L);
        assertThat(call(card, "centsPerEuro", "NOK")).isInstanceOf(IllegalArgumentException.class);
        assertThat(loads.get()).isEqualTo(1);
    }

    @Test
    void theHealthCheckAnswersWhileTheRateServiceIsDown() throws Exception {
        URL classes = RateCard.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader loader = new URLClassLoader(new URL[] {classes}, ClassLoader.getPlatformClassLoader());
        useSource(loader, () -> {
            throw new UncheckedIOException(new IOException("rate service unreachable"));
        });
        Class<?> card = Class.forName("practice.RateCard", false, loader);

        assertThat(call(card, "version")).isEqualTo("rate-card 3.2");

        useSource(loader, () -> RATES);
        assertThat(call(card, "centsPerEuro", "GBP")).isEqualTo(84L);
    }

    @Test
    void aLoadThatFailsIsTriedAgainByTheNextRequest() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        Class<?> card = freshRateCard(() -> {
            if (loads.incrementAndGet() == 1) {
                throw new UncheckedIOException(new IOException("rate service timed out"));
            }
            return RATES;
        });

        Object first = call(card, "centsPerEuro", "SEK");
        Object second = call(card, "centsPerEuro", "SEK");

        assertThat(first).isInstanceOf(UncheckedIOException.class);
        assertThat(((Throwable) first).getCause()).hasMessage("rate service timed out");
        assertThat(second).isEqualTo(1132L);
    }

    @Test
    void requestsThatArriveTogetherAskTheRateServiceOnce() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch firstLoadStarted = new CountDownLatch(1);
        CountDownLatch secondLoadStarted = new CountDownLatch(1);
        CountDownLatch serviceAnswers = new CountDownLatch(1);
        Class<?> card = freshRateCard(() -> {
            if (loads.incrementAndGet() == 1) {
                firstLoadStarted.countDown();
            } else {
                secondLoadStarted.countDown();
            }
            await(serviceAnswers);
            return RATES;
        });

        FutureTask<Object> first = started(card, "centsPerEuro", "CHF");
        assertThat(firstLoadStarted.await(5, TimeUnit.SECONDS)).isTrue();
        FutureTask<Object> second = started(card, "centsPerEuro", "GBP");
        boolean askedTwice = secondLoadStarted.await(2, TimeUnit.SECONDS);
        serviceAnswers.countDown();

        assertThat(outcome(first)).isEqualTo(94L);
        assertThat(outcome(second)).isEqualTo(84L);
        assertThat(askedTwice).isFalse();
        assertThat(loads.get()).isEqualTo(1);
    }
}
