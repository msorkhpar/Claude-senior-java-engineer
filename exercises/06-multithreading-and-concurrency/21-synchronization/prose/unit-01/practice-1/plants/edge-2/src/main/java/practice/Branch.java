package practice;

import java.util.HashMap;
import java.util.Map;

/**
 * One branch of a retail chain. Each branch has one Branch object, shared by the threads of its
 * tills. Invoice numbers come from one sequence for the whole company, and every number goes into
 * the company's journal, in order, together with the branch that issued it.
 */
public class Branch {

    /** The company's invoice journal. Not thread-safe: it takes one line at a time. */
    public interface Journal {
        void append(long number, String branch, String customer);
    }

    /** Fetches a branch's settings from head office. Can take seconds. */
    public interface Settings {
        String load(String branch);
    }

    private static final Map<String, Branch> OPEN = new HashMap<>();
    private static long lastNumber = 10_000;

    private final String name;
    private final String settings;
    private final Journal journal;

    private Branch(String name, String settings, Journal journal) {
        this.name = name;
        this.settings = settings;
        this.journal = journal;
    }

    /** Opens a branch, loading its settings, or returns the branch already open under that name. */
    public static synchronized Branch open(String name, Settings settings, Journal journal) {
        Branch open = OPEN.get(name);
        if (open == null) {
            open = new Branch(name, settings.load(name), journal);
            OPEN.put(name, open);
        }
        return open;
    }

    public String name() {
        return name;
    }

    public String settings() {
        return settings;
    }

    private static final Object SEQUENCE = new Object();

    /** Issues the company's next invoice number to a customer of this branch, and journals it. */
    public long issue(String customer) {
        synchronized (new Object()) {
            long number = ++lastNumber;
            journal.append(number, name, customer);
            return number;
        }
    }
}
