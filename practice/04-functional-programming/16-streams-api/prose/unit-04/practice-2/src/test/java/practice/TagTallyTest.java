package practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TagTallyTest {

    private static final String[] TAGS = {"vegan", "gift", "handmade", "clearance", "organic", "bulk", "local-pickup"};

    private static final ExecutorService CALLER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tally-caller");
        t.setDaemon(true);
        return t;
    });

    @AfterAll
    static void stop() {
        CALLER.shutdownNow();
    }

    /** Runs the reader's code on another thread, so a call that never returns fails the test instead of hanging it. */
    private static <T> T within20s(Callable<T> call) throws Exception {
        return CALLER.submit(call).get(20, TimeUnit.SECONDS);
    }

    /** A large catalogue: tag i % 7, with every third listing also tagged "gift". */
    private static List<String> catalogue(int listings) {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < listings; i++) {
            tags.add(TAGS[i % TAGS.length]);
            if (i % 3 == 0) tags.add("gift");
        }
        return tags;
    }

    private static Map<String, Integer> expected(List<String> tags) {
        Map<String, Integer> counts = new HashMap<>();
        for (String t : tags) counts.merge(t, 1, Integer::sum);
        return counts;
    }

    @Test
    void aSmallCatalogueIsTalliedAndCountsCanBeAddedTogether() throws Exception {
        List<String> tags = List.of("bulk", "vegan", "bulk", "organic", "bulk");

        TagTally.Counts counts = within20s(() -> TagTally.tally(tags));

        assertThat(counts.asMap()).isEqualTo(Map.of("bulk", 3, "vegan", 1, "organic", 1));

        TagTally.Counts more = within20s(() -> TagTally.tally(List.of("handmade", "clearance", "handmade")));
        within20s(() -> { counts.addAll(more); return null; });

        assertThat(counts.asMap()).isEqualTo(Map.of("bulk", 3, "vegan", 1, "organic", 1, "handmade", 2, "clearance", 1));
        assertThat(more.asMap()).isEqualTo(Map.of("handmade", 2, "clearance", 1));
    }

    @Test
    void aLargeCatalogueIsTalliedExactly() throws Exception {
        List<String> tags = catalogue(30_000);

        TagTally.Counts counts = within20s(() -> TagTally.tally(tags));

        assertThat(counts.asMap()).isEqualTo(expected(tags));
    }

    @Test
    void addingCountsThatShareATagKeepsBothCounts() throws Exception {
        TagTally.Counts left = within20s(() -> TagTally.tally(List.of("vegan", "gift", "vegan")));
        TagTally.Counts right = within20s(() -> TagTally.tally(List.of("gift", "gift", "bulk")));

        within20s(() -> { left.addAll(right); return null; });

        assertThat(left.asMap()).isEqualTo(Map.of("vegan", 2, "gift", 3, "bulk", 1));
    }

    @Test
    void aLargeCatalogueLosesNoTag() throws Exception {
        List<String> tags = catalogue(12_000);

        TagTally.Counts counts = within20s(() -> TagTally.tally(tags));

        int total = counts.asMap().values().stream().mapToInt(Integer::intValue).sum();
        assertThat(total).isEqualTo(tags.size());
    }
}
