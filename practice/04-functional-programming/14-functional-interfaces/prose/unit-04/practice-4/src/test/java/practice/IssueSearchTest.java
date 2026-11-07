package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.IssueSearch.Priority.*;

class IssueSearchTest {

    private static final List<IssueSearch.Issue> BOARD = List.of(
            new IssueSearch.Issue(101, "Jane Doe", HIGH, Set.of("backend", "billing")),
            new IssueSearch.Issue(102, "John Roe", LOW, Set.of("frontend")),
            new IssueSearch.Issue(103, "Jane Doe", LOW, Set.of("docs")),
            new IssueSearch.Issue(104, "Max Mustermann", URGENT, Set.of("backend")),
            new IssueSearch.Issue(105, "John Roe", MEDIUM, Set.of()),
            new IssueSearch.Issue(106, "Jane Doe", URGENT, Set.of("frontend", "a11y")));

    private static List<Integer> ids(List<IssueSearch.Issue> issues) {
        return issues.stream().map(IssueSearch.Issue::id).toList();
    }

    private static IssueSearch.Form form(String assignee, IssueSearch.Priority atLeast, String... labels) {
        return new IssueSearch.Form(Optional.ofNullable(assignee), Optional.ofNullable(atLeast), Set.of(labels));
    }

    @Test
    void findsTheIssuesThatMeetEveryFilledInField() {
        assertThat(ids(IssueSearch.search(BOARD, form("Jane Doe", HIGH, "backend")))).containsExactly(101);
    }

    @Test
    void findsIssuesByOneLabelAndAPriority() {
        assertThat(ids(IssueSearch.search(BOARD, form(null, MEDIUM, "frontend")))).containsExactly(106);
    }

    @Test
    void aBlankFormListsEveryIssue() {
        assertThat(ids(IssueSearch.search(BOARD, form(null, null)))).containsExactly(101, 102, 103, 104, 105, 106);
    }

    @Test
    void noLabelTickedMeansNoLabelFilter() {
        assertThat(ids(IssueSearch.search(BOARD, form("John Roe", null)))).containsExactly(102, 105);
    }

    @Test
    void anIssueWithAnyOfTheTickedLabelsIsFound() {
        assertThat(ids(IssueSearch.search(BOARD, form(null, null, "docs", "a11y", "billing")))).containsExactly(101, 103, 106);
    }
}
