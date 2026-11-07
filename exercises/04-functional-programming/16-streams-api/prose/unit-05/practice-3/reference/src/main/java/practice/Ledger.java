package practice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the monthly statements from the ledger. The ledger hands over its lines as a stream,
 * in posting order, and for a large month that stream is parallel.
 */
public class Ledger {

    /** One posted line: the account, the amount in cents, and its posting number. */
    public record Line(String account, long cents, long postingNo) { }

    /** Each account's lines, in posting order: one statement per account. */
    public static Map<String, List<Line>> statements(Stream<Line> lines) {
        return lines.collect(Collectors.groupingBy(Line::account));
    }

    /** The first line posted to each account in the month: the account's opening line. */
    public static Map<String, Line> openingLines(Stream<Line> lines) {
        return lines.collect(Collectors.toMap(Line::account, line -> line, (earlier, later) -> earlier));
    }
}
