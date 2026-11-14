package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            try (Connection db = h2("billing")) {
                try (Statement st = db.createStatement()) {
                    st.execute("create table invoice (id bigint primary key, customer varchar(60))");
                    st.execute("create table invoice_line (invoice_id bigint references invoice(id), line_no int,"
                            + " sku varchar(20), qty int, primary key (invoice_id, line_no))");
                    st.execute("insert into invoice values (902, 'Maple Cafe'), (901, 'Birch Books'), (903, 'Cedar Gym')");
                    st.execute("insert into invoice_line values (901, 2, 'BOOKEND', 4), (901, 1, 'SHELF-90', 1),"
                            + " (903, 1, 'KETTLEBELL-12', 6)");
                }
                InvoiceRepository repository = new InvoiceRepository(counting(db));
                for (InvoiceRepository.Invoice invoice : repository.loadAll()) {
                    System.out.println(invoice.id + " " + invoice.customer + ": " + invoice.lines);
                }
                System.out.println("statements for loadAll: " + executed);
                executed = 0;
                repository.loadAllSlowly();
                System.out.println("statements for loadAllSlowly: " + executed);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static int executed;

    /** A connection that counts the statements it executes, like the tests' one. */
    private static Connection counting(Connection real) {
        return (Connection) Proxy.newProxyInstance(TryIt.class.getClassLoader(), new Class<?>[] {Connection.class},
                (p, m, args) -> {
                    Object result = call(m, real, args);
                    if (!(result instanceof Statement st)) return result;
                    return Proxy.newProxyInstance(TryIt.class.getClassLoader(), new Class<?>[] {m.getReturnType()},
                            (sp, sm, sargs) -> {
                                if (sm.getName().startsWith("execute")) executed++;
                                return call(sm, st, sargs);
                            });
                });
    }

    private static Object call(Method m, Object target, Object[] args) throws Throwable {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /** The practice's database, H2 in memory: Run sees the tests' libraries too. */
    private static Connection h2(String name) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:" + name);
    }
}
