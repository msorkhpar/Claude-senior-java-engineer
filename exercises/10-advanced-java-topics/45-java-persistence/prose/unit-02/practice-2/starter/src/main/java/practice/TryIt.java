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
            try (Connection db = h2("accounts")) {
                try (Statement st = db.createStatement()) {
                    st.execute("create table customer (id bigint primary key, name varchar(60), tier varchar(20), tags varchar(200))");
                    st.execute("insert into customer values (61, 'Rosa Vidal', 'bronze', 'vip,eu'), (62, 'Tariq Noor', 'gold', '')");
                }
                CustomerSession request = new CustomerSession(counting(db));
                CustomerSession.Customer forPricing = request.find(61);
                CustomerSession.Customer forProfile = request.find(61);
                forProfile.tier = "silver";
                System.out.println("same object in one request: " + (forPricing == forProfile));
                System.out.println("pricing sees tier: " + forPricing.tier + ", tags " + forPricing.tags);
                System.out.println("statements for two finds of 61: " + executed);
                System.out.println("unknown id 70: " + request.find(70));
                CustomerSession next = new CustomerSession(counting(db));
                System.out.println("next request reads tier: " + next.find(61).tier + ", same object: " + (next.find(61) == forPricing));
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
