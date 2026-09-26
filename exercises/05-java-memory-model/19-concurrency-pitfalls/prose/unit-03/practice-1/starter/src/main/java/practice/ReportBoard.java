package practice;

import java.util.Optional;

public final class ReportBoard {

    public record Report(String title, int total) {
    }

    /** Publishes the report once; a second call throws IllegalStateException. */
    public void publish(String title, int total) {
        throw new UnsupportedOperationException("write publish");
    }

    /** The published report, or empty before publish. */
    public Optional<Report> read() {
        throw new UnsupportedOperationException("write read");
    }
}
