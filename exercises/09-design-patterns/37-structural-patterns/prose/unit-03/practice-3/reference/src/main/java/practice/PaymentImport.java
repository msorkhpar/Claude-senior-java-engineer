package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Imports a bank's payment file, one CSV row per payment: {@code account,amount}. */
public final class PaymentImport {

    private PaymentImport() {
    }

    /** Marks an interface method that must run in a transaction of its own. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Transactional {
    }

    public interface Rows {
        /** Imports every row; a row that can't be imported is rejected and the others go on. */
        void importAll(List<String> rows);

        /** Imports one row, or throws IllegalArgumentException and imports none of it. */
        @Transactional
        void importRow(String row);

        List<String> rejected();
    }

    /**
     * The ledger the payments are written to. A write outside a transaction is final at once; a
     * write inside one becomes final on commit and disappears on rollback.
     */
    public static final class Ledger {
        private final List<String> entries = new ArrayList<>();
        private List<String> pending;
        private int commits;
        private int rollbacks;

        public void begin() {
            if (pending != null) throw new IllegalStateException("a transaction is already open");
            pending = new ArrayList<>();
        }

        public void commit() {
            entries.addAll(pending);
            pending = null;
            commits++;
        }

        public void rollback() {
            pending = null;
            rollbacks++;
        }

        public void write(String entry) {
            if (pending != null) pending.add(entry);
            else entries.add(entry);
        }

        public List<String> entries() {
            return List.copyOf(entries);
        }

        public int commits() {
            return commits;
        }

        public int rollbacks() {
            return rollbacks;
        }
    }

    /** Runs each call to a {@code @Transactional} method in a transaction on {@code ledger}. */
    public static <T> T transactional(T target, Class<T> type, Ledger ledger) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> {
                    boolean tx = method.isAnnotationPresent(Transactional.class);
                    if (tx) ledger.begin();
                    try {
                        Object result = method.invoke(target, args);
                        if (tx) ledger.commit();
                        return result;
                    } catch (InvocationTargetException e) {
                        if (tx) ledger.rollback();
                        throw e.getCause();
                    }
                }));
    }

    public static final class CsvRows implements Rows {
        private final Ledger ledger;
        private final List<String> rejected = new ArrayList<>();
        private Rows self;

        CsvRows(Ledger ledger) {
            this.ledger = ledger;
        }

        @Override
        public void importAll(List<String> rows) {
            for (String row : rows) {
                try {
                    self.importRow(row);
                } catch (IllegalArgumentException e) {
                    rejected.add(row);
                }
            }
        }

        @Override
        public void importRow(String row) {
            String[] fields = row.split(",", -1);
            ledger.write("payment from " + fields[0]);
            if (fields.length < 2 || fields[1].isBlank()) throw new IllegalArgumentException("no amount: " + row);
            ledger.write("amount " + fields[1]);
        }

        @Override
        public List<String> rejected() {
            return List.copyOf(rejected);
        }
    }

    /** Builds the importer the application calls. */
    public static Rows create(Ledger ledger) {
        CsvRows rows = new CsvRows(ledger);
        Rows proxy = transactional(rows, Rows.class, ledger);
        rows.self = proxy;
        return proxy;
    }
}
