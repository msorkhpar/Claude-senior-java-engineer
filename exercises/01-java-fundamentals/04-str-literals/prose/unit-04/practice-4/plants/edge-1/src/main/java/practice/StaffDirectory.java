package practice;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * The company's staff directory. Each office prints its own list, in the language of the office,
 * from one server whose default locale is whatever the data centre set.
 */
public class StaffDirectory {

    private final List<String> names = new ArrayList<>();

    public void hire(String name) {
        names.add(name);
    }

    /** The printed list for an office in {@code locale}: every person, in that office's order. */
    public List<String> printedList(Locale locale) {
        return sortForDisplay(names, locale);
    }

    /** {@code names} in the order a reader in {@code locale} expects; nobody is left out. */
    static List<String> sortForDisplay(Collection<String> names, Locale locale) {
        List<String> sorted = new ArrayList<>(names);
        sorted.sort(null);
        return sorted;
    }
}
