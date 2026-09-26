package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ServiceRegistryTest {

    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    /** Waits, at most 20 s, until t has finished or waits for a lock that owner holds; true if it waits on owner. */
    private static boolean waitsOn(Thread t, Thread owner) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            if (t.getState() == Thread.State.TERMINATED) {
                return false;
            }
            ThreadInfo info = THREADS.getThreadInfo(t.threadId());
            if (info != null && info.getLockOwnerId() == owner.threadId()) {
                return true;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return false;
    }

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** A name built at run time, so == against a literal cannot pass for equals. */
    private static String fresh(String text) {
        return new String(text.toCharArray());
    }

    @Test
    void registersLooksUpAndUnregisters() {
        ServiceRegistry registry = new ServiceRegistry();
        assertThat(registry.register("auth", "http://auth:8080")).isTrue();
        assertThat(registry.register("users", "http://users:8081")).isTrue();
        assertThat(registry.lookup(fresh("auth"))).contains("http://auth:8080");
        assertThat(registry.lookup("nope")).isEmpty();
        assertThat(registry.names()).containsExactlyInAnyOrder("auth", "users");
        assertThat(registry.resolve("billing", n -> "http://" + n + ":9000")).isEqualTo("http://billing:9000");
        assertThat(registry.lookup("billing")).contains("http://billing:9000");
        assertThat(registry.resolve(fresh("auth"), n -> "http://wrong")).isEqualTo("http://auth:8080");
        assertThat(registry.unregister(fresh("users"))).isTrue();
        assertThat(registry.unregister("nope")).isFalse();
        assertThat(registry.names()).containsExactlyInAnyOrder("auth", "billing");
    }

    @Test
    void aDuplicateRegistrationKeepsTheFirst() {
        ServiceRegistry registry = new ServiceRegistry();
        assertThat(registry.register("auth", "http://auth:8080")).isTrue();
        assertThat(registry.register(fresh("auth"), "http://other:1")).isFalse();
        assertThat(registry.lookup(fresh("auth"))).contains("http://auth:8080");
    }

    @Test
    void racingResolvesCallTheResolverOnce() throws InterruptedException {
        ServiceRegistry registry = new ServiceRegistry();
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<String> a = new AtomicReference<>();
        AtomicReference<String> b = new AtomicReference<>();
        Thread first = daemon(() -> a.set(registry.resolve("billing", n -> {
            int call = calls.incrementAndGet();
            if (call == 1) {
                entered.countDown();
                try {
                    release.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return "http://billing:900" + call;
        })));
        assertThat(entered.await(20, TimeUnit.SECONDS)).isTrue();
        Thread second = daemon(() -> b.set(registry.resolve(fresh("billing"), n -> "http://billing:900" + calls.incrementAndGet())));
        waitsOn(second, first);
        int callsWhileTheFirstResolves = calls.get();
        release.countDown();
        first.join(20_000);
        second.join(20_000);
        assertThat(first.isAlive() || second.isAlive()).isFalse();
        assertThat(callsWhileTheFirstResolves).as("the second caller waited for the first resolution").isEqualTo(1);
        assertThat(a.get()).isEqualTo("http://billing:9001");
        assertThat(b.get()).isEqualTo("http://billing:9001");
        assertThat(registry.lookup("billing")).contains("http://billing:9001");
    }
}
