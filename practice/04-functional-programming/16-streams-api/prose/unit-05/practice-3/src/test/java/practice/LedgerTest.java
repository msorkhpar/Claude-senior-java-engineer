package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerTest {

    private static final List<String> ACCOUNTS = List.of("CH93-0076", "GB29-6016", "SE45-5000");
    private static final int SIZE = 600;

    /** The ledger runs in a pool of four threads the test owns, so it never depends on the runner's CPU count. */
    private final ForkJoinPool pool = new ForkJoinPool(4);

    @AfterEach
    void stopThePool() {
        pool.shutdownNow();
    }

    /**
     * A month of postings. The second-to-last line goes to the same account as line 1, and the
     * last line goes to another account.
     */
    private static List<Ledger.Line> month() {
        List<Ledger.Line> lines = new ArrayList<>();
        for (int i = 0; i < SIZE - 2; i++) {
            lines.add(new Ledger.Line(ACCOUNTS.get((i * 7 + 2) % 3), 100L + 37L * i, 81_000L + i));
        }
        lines.add(new Ledger.Line(lines.get(1).account(), 9_999L, 81_000L + SIZE - 2));
        lines.add(new Ledger.Line(lines.get(0).account(), 1_234L, 81_000L + SIZE - 1));
        assertThat(lines.get(0).account()).isNotEqualTo(lines.get(1).account());
        return lines;
    }

    /**
     * A parallel stream over the month in which line 1 is held back until the last line has been
     * handed on, so the second-to-last line, in the same account, is handled before it.
     */
    private static Stream<Ledger.Line> parallelWithLine1Late(List<Ledger.Line> lines) {
        Ledger.Line held = lines.get(1);
        Ledger.Line last = lines.get(lines.size() - 1);
        CountDownLatch lastHandedOn = new CountDownLatch(1);
        return lines.parallelStream().peek(line -> {
            if (line == last) lastHandedOn.countDown();
            if (line == held) {
                try {
                    lastHandedOn.await(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private <T> T inPool(Function<Stream<Ledger.Line>, T> ledger, Stream<Ledger.Line> lines) throws Exception {
        return pool.submit(() -> ledger.apply(lines)).get(20, TimeUnit.SECONDS);
    }

    private static Map<String, List<Ledger.Line>> expectedStatements(List<Ledger.Line> lines) {
        Map<String, List<Ledger.Line>> expected = new java.util.HashMap<>();
        for (Ledger.Line line : lines) expected.computeIfAbsent(line.account(), a -> new ArrayList<>()).add(line);
        return expected;
    }

    @Test
    void groupsASequentialMonthInPostingOrder() throws Exception {
        List<Ledger.Line> lines = month();

        assertThat(inPool(Ledger::statements, lines.stream())).isEqualTo(expectedStatements(lines));
        assertThat(inPool(Ledger::openingLines, lines.stream())).containsOnly(
                Map.entry(lines.get(0).account(), lines.get(0)),
                Map.entry(lines.get(1).account(), lines.get(1)),
                Map.entry(lines.get(2).account(), lines.get(2)));
    }

    @Test
    void groupsAParallelMonthCompletely() throws Exception {
        List<Ledger.Line> lines = month();

        Map<String, List<Ledger.Line>> statements = inPool(Ledger::statements, lines.parallelStream());
        Map<String, Ledger.Line> opening = inPool(Ledger::openingLines, lines.parallelStream());

        assertThat(statements.keySet()).containsExactlyInAnyOrderElementsOf(ACCOUNTS);
        expectedStatements(lines).forEach((account, expected) ->
                assertThat(new HashSet<>(statements.get(account))).isEqualTo(new HashSet<>(expected)));
        assertThat(opening.keySet()).containsExactlyInAnyOrderElementsOf(ACCOUNTS);
        assertThat(opening.values()).allSatisfy(line -> assertThat(lines).contains(line));
    }

    @Test
    void aStatementKeepsPostingOrderWhenALaterLineIsHandledFirst() throws Exception {
        List<Ledger.Line> lines = month();

        Map<String, List<Ledger.Line>> statements = inPool(Ledger::statements, parallelWithLine1Late(lines));

        String account = lines.get(1).account();
        assertThat(statements.get(account)).containsExactlyElementsOf(expectedStatements(lines).get(account));
    }

    @Test
    void theOpeningLineIsTheFirstPostedWhenALaterLineIsHandledFirst() throws Exception {
        List<Ledger.Line> lines = month();

        Map<String, Ledger.Line> opening = inPool(Ledger::openingLines, parallelWithLine1Late(lines));

        assertThat(opening.get(lines.get(1).account())).isEqualTo(lines.get(1));
    }
}
