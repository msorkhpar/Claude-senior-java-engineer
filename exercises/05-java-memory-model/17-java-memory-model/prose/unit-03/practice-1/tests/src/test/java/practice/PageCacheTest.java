package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PageCacheTest {

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void fetchesOnceAndCaches() {
        AtomicInteger fetches = new AtomicInteger();
        PageCache cache = new PageCache(url -> {
            fetches.incrementAndGet();
            return "<page " + url + ">";
        });
        assertThat(cache.cached("a")).isNull();
        assertThat(cache.load("a")).isEqualTo("<page a>");
        assertThat(cache.load("a")).isEqualTo("<page a>");
        assertThat(fetches.get()).isEqualTo(1);
        // A key built at run time must find the page too: the cache compares URLs by value.
        assertThat(cache.cached(new String("a"))).isEqualTo("<page a>");
        assertThat(cache.load(new String("a"))).isEqualTo("<page a>");
        assertThat(fetches.get()).isEqualTo(1);
        assertThat(cache.load("b")).isEqualTo("<page b>");
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void aSlowFetchDoesNotBlockReaders() throws Exception {
        CountDownLatch fetching = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        PageCache cache = new PageCache(url -> {
            if (url.equals("slow")) {
                fetching.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return "<page " + url + ">";
        });
        cache.load("a");
        Thread a = daemon(() -> cache.load("slow"));
        assertThat(fetching.await(5, TimeUnit.SECONDS)).as("thread A is fetching").isTrue();
        AtomicReference<String> read = new AtomicReference<>();
        Thread b = daemon(() -> read.set(cache.cached("a")));
        b.join(2000);
        boolean bWaited = b.isAlive();
        release.countDown();
        a.join(5000);
        b.join(5000);
        assertThat(bWaited).as("the reader had to wait for the fetch").isFalse();
        assertThat(read.get()).isEqualTo("<page a>");
        assertThat(cache.cached("slow")).isEqualTo("<page slow>");
    }

    @Test
    void outsideLockingCannotStallTheCache() throws Exception {
        PageCache cache = new PageCache(url -> "<page " + url + ">");
        cache.load("a");
        AtomicReference<String> read = new AtomicReference<>();
        Thread b;
        boolean bWaited;
        synchronized (cache) {
            b = daemon(() -> read.set(cache.cached("a")));
            b.join(2000);
            bWaited = b.isAlive();
        }
        b.join(5000);
        assertThat(bWaited).as("the cache waited for a lock held on the cache object").isFalse();
        assertThat(read.get()).isEqualTo("<page a>");
    }

    @Test
    void theLockIsAPrivateFinalField() {
        PageCache cache = new PageCache(url -> "<page " + url + ">");
        assertThat(cache.load("a")).isEqualTo("<page a>");
        List<Field> fields = Arrays.stream(PageCache.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()) && !f.isSynthetic())
                .toList();
        assertThat(fields).isNotEmpty();
        assertThat(fields).allSatisfy(f -> {
            assertThat(Modifier.isPrivate(f.getModifiers())).as(f.getName() + " is private").isTrue();
            assertThat(Modifier.isFinal(f.getModifiers())).as(f.getName() + " is final").isTrue();
        });
    }
}
