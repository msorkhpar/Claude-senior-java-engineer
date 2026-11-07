package practice;

import java.util.Set;

/**
 * Reads enum values out of the warehouse's CSV imports, for whichever enum a column holds, and
 * moves a value on to the next one for the dashboard's "advance" button.
 */
public final class Enums {

    private Enums() {
    }

    /**
     * The constant of {@code type} that {@code text} names, as people type it into a spreadsheet:
     * any case, spaces around it, and a hyphen or a space where the name has an underscore.
     *
     * @throws IllegalArgumentException when no constant matches
     */
    public static <E extends Enum<E>> E parse(Class<E> type, String text) {
        // TODO: Find the constant of `type` that the cell's text means. Text that names no
        //       constant is refused with IllegalArgumentException.
        throw new IllegalStateException("TODO: parse");
    }

    /** Every constant a comma-separated cell names, each once. */
    public static <E extends Enum<E>> Set<E> parseAll(Class<E> type, String cell) {
        Set<E> result = newSet(type);
        for (String part : cell.split(",")) {
            result.add(parse(type, part));
        }
        return result;
    }

    /** The empty set parseAll fills. */
    static <E extends Enum<E>> Set<E> newSet(Class<E> type) {
        // TODO: The import's report lists a cell's flags in this set's order, and the warehouse
        //       wants them in the order the enum declares them, whatever order the cell has.
        throw new IllegalStateException("TODO: newSet");
    }

    /** The constant declared after {@code current}; after the last one, the first. */
    public static <E extends Enum<E>> E next(E current) {
        // TODO: Answer the constant declared after `current`, wrapping round to the first,
        //       for every constant of every enum.
        throw new IllegalStateException("TODO: next");
    }
}
