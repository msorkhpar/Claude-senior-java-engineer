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
        if (!form.anyOfLabels().isEmpty()) {
            criteria.add(anyLabel(form.anyOfLabels()));
        }
        return allOf(criteria);
    }

    /** An issue that carries at least one of these labels. */
    static Predicate<Issue> anyLabel(Set<String> labels) {
        return labels.stream()
                .map(label -> (Predicate<Issue>) issue -> issue.labels().contains(label))
                .reduce(issue -> false, Predicate::or);
    }

    /** An issue that meets every one of these criteria. */
    static Predicate<Issue> allOf(List<Predicate<Issue>> criteria) {
        return criteria.stream().reduce(Predicate::and).orElse(issue -> false);
    }
}
