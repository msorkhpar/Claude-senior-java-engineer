package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class DiscussionTest {

    private static final int CHAIN = 250_000;

    /** A worker thread's ordinary stack: 1 MB, whatever the runner's -Xss is. */
    private static <T> T onAWorkerThread(Callable<T> export) throws InterruptedException {
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread worker = new Thread(null, () -> {
            try {
                outcome.set(export.call());
            } catch (Throwable t) {
                outcome.set(t);
            }
        }, "export-worker", 1L << 20);
        worker.setDaemon(true);
        worker.start();
        worker.join(TimeUnit.SECONDS.toMillis(60));
        if (worker.isAlive()) fail("the export was still running after 60 s");
        Object result = outcome.get();
        if (result instanceof Throwable t) fail("the export failed with " + t.getClass().getName(), t);
        @SuppressWarnings("unchecked") T value = (T) result;
        return value;
    }

    /** A spam bot answering itself: every comment has exactly one reply. */
    private static Discussion.Comment chainOf(int comments) {
        Discussion.Comment root = new Discussion.Comment("bot-0");
        Discussion.Comment last = root;
        for (int i = 1; i < comments; i++) last = last.reply("bot-" + i);
        return root;
    }

    /**
     * r
     * +- a
     * |  +- a1
     * |  |  +- a1x
     * |  +- a2
     * +- b
     * +- c
     *    +- c1
     */
    private static Discussion.Comment branching() {
        Discussion.Comment r = new Discussion.Comment("r");
        Discussion.Comment a = r.reply("a");
        a.reply("a1").reply("a1x");
        a.reply("a2");
        r.reply("b");
        r.reply("c").reply("c1");
        return r;
    }

    @Test
    void summarisesAShortDiscussion() throws InterruptedException {
        Discussion.Comment root = branching();

        assertThat(onAWorkerThread(() -> Discussion.deepest(root))).isEqualTo(4);
        List<String> ids = onAWorkerThread(() -> Discussion.inOrder(root));
        assertThat(ids).first().isEqualTo("r");
        assertThat(ids).containsExactlyInAnyOrder("r", "a", "a1", "a1x", "a2", "b", "c", "c1");
        assertThat(onAWorkerThread(() -> Discussion.summary(root))).isEqualTo("8 comments, 4 levels");
    }

    @Test
    void aCommentWithNoRepliesIsOneLevel() throws InterruptedException {
        Discussion.Comment alone = new Discussion.Comment("lonely-7");

        assertThat(onAWorkerThread(() -> Discussion.deepest(alone))).isEqualTo(1);
        assertThat(onAWorkerThread(() -> Discussion.inOrder(alone))).containsExactly("lonely-7");
    }

    @Test
    void measuresAChainOfRepliesAQuarterMillionDeep() throws InterruptedException {
        Discussion.Comment root = chainOf(CHAIN);

        assertThat(onAWorkerThread(() -> Discussion.deepest(root))).isEqualTo(CHAIN);
    }

    @Test
    void listsAChainOfRepliesAQuarterMillionDeep() throws InterruptedException {
        Discussion.Comment root = chainOf(CHAIN);

        List<String> ids = onAWorkerThread(() -> Discussion.inOrder(root));
        assertThat(ids).hasSize(CHAIN);
        assertThat(ids.get(0)).isEqualTo("bot-0");
        assertThat(ids.get(CHAIN / 2)).isEqualTo("bot-" + CHAIN / 2);
        assertThat(ids.get(CHAIN - 1)).isEqualTo("bot-" + (CHAIN - 1));
    }

    @Test
    void listsRepliesOldestFirstEachFollowedByItsOwnThread() throws InterruptedException {
        Discussion.Comment root = branching();

        assertThat(onAWorkerThread(() -> Discussion.inOrder(root)))
                .containsExactly("r", "a", "a1", "a1x", "a2", "b", "c", "c1");
    }
}
