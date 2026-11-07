package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exports records to CSV for the finance team's spreadsheets. A plain export has one header line
 * and one line per record. A family export takes the sealed interface the records implement and
 * writes one section per record type of that family, empty sections included, so every month's
 * file has the same sections.
 */
public class CsvExport {

    /** One header line, then one line per record. All records are of the same type. */
    public static String export(Class<? extends Record> type, List<? extends Record> rows) {
        StringBuilder out = new StringBuilder(String.join(",", columns(type))).append('\n');
        for (Record r : rows) out.append(line(r)).append('\n');
        return out.toString();
    }

    /** One "## TypeName" section per record type of the sealed family, sorted by type name. */
    public static String exportFamily(Class<?> sealedRoot, List<? extends Record> rows) {
        List<Class<? extends Record>> types = new ArrayList<>(recordTypes(sealedRoot));
        types.sort(Comparator.comparing(Class::getSimpleName));
        StringBuilder out = new StringBuilder();
        for (Class<? extends Record> type : types) {
            out.append("## ").append(type.getSimpleName()).append('\n');
            out.append(export(type, rows.stream().filter(type::isInstance).toList()));
        }
        return out.toString();
    }

    private static String line(Record r) {
        return values(r).stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /** The column names of a record type. */
    static List<String> columns(Class<? extends Record> type) {
        // TODO: One column per value the record type holds, nothing else, in the same order
        //       on every JVM: the order the record's author declared them in.
        throw new IllegalStateException("TODO: columns");
    }

    /** One record's values, in the order of columns(...) for its type. */
    static List<Object> values(Record r) {
        List<Object> values = new ArrayList<>();
        try {
            for (String column : columns(r.getClass())) {
                values.add(valueOf(r, column));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return values;
    }

    /** The value of one column of one record. */
    static Object valueOf(Record r, String column) throws ReflectiveOperationException {
        // TODO: The record's value in this column.
        throw new IllegalStateException("TODO: valueOf");
    }

    /** Every record type a value of the sealed family can have. */
    static Set<Class<? extends Record>> recordTypes(Class<?> sealedRoot) {
        // TODO: The family may nest: a case can itself be a sealed interface with cases of its
        //       own. Return the record types only.
        throw new IllegalStateException("TODO: recordTypes");
    }
}
