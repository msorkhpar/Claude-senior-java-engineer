package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/** The issue tracker's search form. */
public class IssueSearch {

    public enum Priority { LOW, MEDIUM, HIGH, URGENT }

    public record Issue(int id, String assignee, Priority priority, Set<String> labels) {}

    /**
     * What the user filled in. An empty Optional is a field left blank; an empty label set means
     * no label box was ticked.
     */
    public record Form(Optional<String> assignee, Optional<Priority> atLeast, Set<String> anyOfLabels) {}

    public static List<Issue> search(List<Issue> issues, Form form) {
        return issues.stream().filter(matching(form)).toList();
    }

    /** One predicate for the whole form: an issue must meet every field the user filled in. */
    static Predicate<Issue> matching(Form form) {
        List<Predicate<Issue>> criteria = new ArrayList<>();
        form.assignee().ifPresent(name -> criteria.add(issue -> name.equals(issue.assignee())));
        form.atLeast().ifPresent(level -> criteria.add(issue -> issue.priority().compareTo(level) >= 0));
        // TODO: Add the label criterion: the issue carries any one of the ticked labels.
        return allOf(criteria);
    }

    /** An issue that carries at least one of these labels. */
    static Predicate<Issue> anyLabel(Set<String> labels) {
        // TODO: Build one predicate that holds when an issue carries at least one of the labels.
        throw new IllegalStateException("TODO: anyLabel");
    }

    /** An issue that meets every one of these criteria. */
    static Predicate<Issue> allOf(List<Predicate<Issue>> criteria) {
        // TODO: Combine the criteria into one predicate that holds when every one of them holds.
        throw new IllegalStateException("TODO: allOf");
    }
}
