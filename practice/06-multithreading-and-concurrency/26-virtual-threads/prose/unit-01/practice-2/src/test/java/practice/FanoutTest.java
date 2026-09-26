package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FanoutTest {

    @Test
    void returnsResultsInKeyOrder() throws Exception {
        assertThat(Fanout.fetchAll(List.of("a", "b", "c"), String::toUpperCase)).containsExactly("A", "B", "C");
    }

    /** Each fetch arrives, then waits (at most 4 s) until all thousand have arrived; no pool of any usual size lets that happen. */
    @Test
    void allFetchesAreInFlightAtOnce() throws Exception {
        List<String> keys = IntStream.range(0, 1000).mapToObj(i -> "k" + i).toList();
        CountDownLatch arrived = new CountDownLatch(keys.size());
        List<String> results = Fanout.fetchAll(keys, key -> {
            arrived.countDown();
            try {
                return arrived.await(4, TimeUnit.SECONDS) ? key : "alone";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "interrupted";
            }
        });
        assertThat(results).as("fetches that never saw all the others in flight").containsExactlyElementsOf(keys);
    }

    @Test
    void eachFetchRunsOnItsOwnVirtualThread() throws Exception {
        Set<Thread> seen = ConcurrentHashMap.newKeySet();
        List<String> results = Fanout.fetchAll(List.of("a", "b", "c", "d", "e"), key -> {
            Thread me = Thread.currentThread();
            seen.add(me);
            return key + ":" + me.isVirtual();
        });
        assertThat(results).containsExactly("a:true", "b:true", "c:true", "d:true", "e:true");
        assertThat(seen).hasSize(5);
    }

    @Test
    void aFailedFetchFailsTheCall() {
        assertThatThrownBy(() -> Fanout.fetchAll(List.of("a", "b"), key -> {
            if (key.equals("b")) {
                throw new IllegalStateException("no b today");
            }
            return key;
        })).isInstanceOf(ExecutionException.class)
                .cause().isInstanceOf(IllegalStateException.class).hasMessage("no b today");
    }

    @Test
    void noCapEvenAtScale() throws Exception {
        List<String> keys = IntStream.range(0, 2500).mapToObj(i -> "k" + i).toList();
        CountDownLatch arrived = new CountDownLatch(keys.size());
        List<String> results = Fanout.fetchAll(keys, key -> {
            arrived.countDown();
            try {
                return arrived.await(3, TimeUnit.SECONDS) ? key : "alone";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "interrupted";
            }
        });
        assertThat(results).as("2500 fetches are in flight at once: there is no cap, not even a large one").containsExactlyElementsOf(keys);
    }
}
