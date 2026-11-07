package practice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Picks payments for the fraud team's review queue from the live payment feed. Every payment the
 * queue takes is scored by the risk service, which is slow and allows few calls per second.
 */
public class PaymentWatch {

    public record Payment(String id, String account, long amountCents) {}

    public record Assessment(Payment payment, int score) {}

    /** The risk service: one remote call per payment. */
    public interface RiskService {
        Assessment assess(Payment payment);
    }

    private final RiskService risk;
    private final long reviewFromCents;

    public PaymentWatch(RiskService risk, long reviewFromCents) {
        this.risk = risk;
        this.reviewFromCents = reviewFromCents;
    }

    /** The sheet the reviewers see: one line per payment, "id score". */
    public String reviewSheet(Stream<Payment> feed, int slots) {
        return nextForReview(feed, slots).stream()
                .map(a -> a.payment().id() + " " + a.score())
                .collect(Collectors.joining("\n"));
    }

    /**
     * The next {@code slots} payments of at least {@code reviewFromCents}, taken in the order they
     * arrive on the feed, each assessed once, highest score first.
     */
    List<Assessment> nextForReview(Stream<Payment> feed, int slots) {
        return feed.filter(p -> p.amountCents() >= reviewFromCents)
                .map(risk::assess)
                .sorted(Comparator.comparingInt(Assessment::score).reversed())
                .limit(slots)
                .toList();
    }
}
