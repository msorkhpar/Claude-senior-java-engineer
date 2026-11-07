package practice;

import java.util.List;
import java.util.Map;
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
        // TODO: A customer reads a statement from top to bottom, so each account's lines keep
        //       the order in which they were posted, also when the stream is parallel.
        throw new IllegalStateException("TODO: statements");
    }

    /** The first line posted to each account in the month: the account's opening line. */
    public static Map<String, Line> openingLines(Stream<Line> lines) {
        // TODO: The opening line is the account's first posting of the month, also when the
        //       stream is parallel.
        throw new IllegalStateException("TODO: openingLines");
    }
}
