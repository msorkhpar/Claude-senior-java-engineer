package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class MemoTest {

    /** Runs the call on a daemon thread, so a call that never returns fails the test instead of hanging it. */
    private static <T> T within5s(java.util.concurrent.Callable<T> call) throws Exception {
        ExecutorService runner = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            Future<T> result = runner.submit(call);
            return result.get(5, TimeUnit.SECONDS);
        } finally {
            runner.shutdownNow();
        }
    }

    @Test
    void eachKeyIsComputedOnce() throws Exception {
        Map<String, AtomicInteger> calls = new HashMap<>();
        Memo<String, Integer> vowels = new Memo<>(word -> {
            calls.computeIfAbsent(word, w -> new AtomicInteger()).incrementAndGet();
            return (int) word.chars().filter(c -> "aeiou".indexOf(c) >= 0).count();
        });

        List<Integer> answers = within5s(() -> {
            List<Integer> out = new ArrayList<>();
            for (String w : List.of("harbour", "quay", "harbour", "estuary", "quay", "harbour")) {
                out.add(vowels.apply(w));
            }
            return out;
        });

        assertThat(answers).containsExactly(3, 2, 3, 3, 2, 3);
        assertThat(calls).hasSize(3);
        assertThat(calls.values()).allSatisfy(n -> assertThat(n.get()).isEqualTo(1));
    }

    @Test
    void aKeyWithNoAnswerIsAlsoComputedOnce() throws Exception {
        AtomicInteger lookups = new AtomicInteger();
        Map<String, String> directory = Map.of("dock-4", "Nina Example");
        Memo<String, String> owner = new Memo<>(berth -> {
            lookups.incrementAndGet();
            return directory.get(berth);
        });

        List<String> answers = within5s(() -> {
            List<String> out = new ArrayList<>();
            for (int i = 0; i < 4; i++) out.add(owner.apply("dock-9"));
            out.add(owner.apply("dock-4"));
            return out;
        });

        assertThat(answers).containsExactly(null, null, null, null, "Nina Example");
        assertThat(lookups).hasValue(2);
    }

    @Test
    void theFunctionCanAskTheMemoForOtherKeys() throws Exception {
        // a hull section costs its own work plus the section it is welded onto
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Function<Integer, Long>> self = new AtomicReference<>();
        Memo<Integer, Long> cost = new Memo<>(section -> {
            calls.incrementAndGet();
            long own = 100L + section;
            return section == 0 ? own : own + self.get().apply(section - 1);
        });
        self.set(cost);

        long total = within5s(() -> cost.apply(40));
        long again = within5s(() -> cost.apply(25));

        assertThat(total).isEqualTo(41 * 100L + 40 * 41 / 2);
        assertThat(again).isEqualTo(26 * 100L + 25 * 26 / 2);
        assertThat(calls).hasValue(41);
    }
}
