package practice;

import java.util.LinkedHashSet;
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
        String wanted = text.trim().replace('-', '_').replace(' ', '_');
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(wanted)) return constant;
        }
        throw new IllegalArgumentException(type.getSimpleName() + " has no constant for '" + text + "'");
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
        return new LinkedHashSet<>();
    }

    /** The constant declared after {@code current}; after the last one, the first. */
    public static <E extends Enum<E>> E next(E current) {
        E[] all = current.getDeclaringClass().getEnumConstants();
        return all[(current.ordinal() + 1) % all.length];
    }
}
