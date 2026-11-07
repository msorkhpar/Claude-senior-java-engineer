package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisitReportTest {

    private static VisitReport.Visit visit(String user, String page, int second) {
        return new VisitReport.Visit(user, page, second);
    }

    private static final List<VisitReport.Visit> RETURNING = List.of(
            visit("u-17", "/pricing", 3), visit("u-04", "/blog/java-21", 8), visit("u-17", "/signup", 15),
            visit("u-91", "/", 21), visit("u-04", "/docs", 30), visit("u-17", "/docs", 44));

    @Test
    void listsEachUserWhoVisitedOnce() {
        List<VisitReport.Visit> visits = List.of(
                visit("u-30", "/careers", 2), visit("u-12", "/pricing", 5), visit("u-77", "/", 9));

        assertThat(new VisitReport().landingVisits(visits)).containsExactlyElementsOf(visits);
    }

    @Test
    void aReturningUserIsListedOnceWithTheirFirstVisit() {
        assertThat(new VisitReport().landingVisits(RETURNING)).containsExactly(
                visit("u-17", "/pricing", 3), visit("u-04", "/blog/java-21", 8), visit("u-91", "/", 21));
    }

    @Test
    void theNextReportListsEveryUserAgain() {
        VisitReport report = new VisitReport();
        List<VisitReport.Visit> morning = List.of(visit("u-51", "/", 1), visit("u-52", "/blog", 4));
        List<VisitReport.Visit> noon = List.of(visit("u-52", "/docs", 7), visit("u-51", "/pricing", 9), visit("u-53", "/", 12));

        report.landingVisits(morning);

        assertThat(report.landingVisits(noon)).containsExactly(
                visit("u-52", "/docs", 7), visit("u-51", "/pricing", 9), visit("u-53", "/", 12));
    }
}
