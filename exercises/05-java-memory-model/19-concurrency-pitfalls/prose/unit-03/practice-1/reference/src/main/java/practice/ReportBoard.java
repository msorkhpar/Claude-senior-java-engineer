package practice;

import java.util.Objects;
import java.util.Optional;

public final class ReportBoard {

    public record Report(String title, int total) {
    }

    private String title;
    private int total;
    private volatile boolean ready;

    /** Publishes the report once; a second call throws IllegalStateException. */
    public void publish(String title, int total) {
        Objects.requireNonNull(title, "title");
        if (ready) {
            throw new IllegalStateException("the board is already published");
        }
        this.title = title;
        this.total = total;
        ready = true;
    }

    /** The published report, or empty before publish. */
    public Optional<Report> read() {
        if (!ready) {
            return Optional.empty();
        }
        return Optional.of(new Report(title, total));
    }
}
