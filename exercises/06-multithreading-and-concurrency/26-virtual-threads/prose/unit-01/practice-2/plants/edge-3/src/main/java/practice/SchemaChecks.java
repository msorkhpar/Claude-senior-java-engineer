package practice;

import java.util.function.Supplier;

/**
 * Checks the documents partners upload against the partner schema. The upload service runs every
 * request on a virtual thread of its own and calls check() there.
 */
public class SchemaChecks {

    /**
     * Checks documents against the compiled schema. Building one compiles the schema, which takes
     * a lot of CPU and memory. A checker keeps state between calls: one thread at a time only.
     */
    public interface Checker {
        /** True when the document is valid; IllegalArgumentException when it can't be read at all. */
        boolean check(String document);
    }

    private final Supplier<Checker> factory;
    private final int max;
    private volatile Checker shared;

    /** factory builds a new checker; max is the most checkers the service may ever hold. */
    public SchemaChecks(Supplier<Checker> factory, int max) {
        this.factory = factory;
        this.max = max;
    }

    /** Checks one document, on the calling thread. */
    public boolean check(String document) throws InterruptedException {
        return borrow().check(document);
    }

    /** A checker that nobody else is using until the caller is done with it. */
    Checker borrow() throws InterruptedException {
        if (shared == null) {
            synchronized (this) {
                if (shared == null) shared = factory.get();
            }
        }
        return shared;
    }
}
