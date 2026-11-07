package practice;

import java.util.Arrays;
import java.util.List;

/** A page sent to the engineers on call. */
public final class Alert {
    private final String title;
    private final List<String> recipients;

    private Alert(String title, List<String> recipients) {
        this.title = title;
        this.recipients = recipients;
    }

    /**
     * An alert with this title for these recipients. The pager shows the recipients in
     * alphabetical order, and an alert keeps the recipients it was created with.
     */
    public static Alert to(String title, String... recipients) {
        String[] sorted = recipients.clone();
        Arrays.sort(sorted);
        return new Alert(title, List.of(sorted));
    }

    public String title() {
        return title;
    }

    /** The recipients, in alphabetical order. */
    public List<String> recipients() {
        return recipients;
    }

    /** The line the pager shows. */
    public String render() {
        return title + " -> " + String.join(", ", recipients());
    }
}
