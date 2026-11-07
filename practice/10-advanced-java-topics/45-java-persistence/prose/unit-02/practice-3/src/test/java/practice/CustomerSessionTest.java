package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerSessionTest {

    private static final AtomicInteger DATABASES = new AtomicInteger();

    private Connection real;
    private final AtomicInteger updates = new AtomicInteger();

    @BeforeEach
    void openDatabase() throws SQLException {
        real = DriverManager.getConnection("jdbc:h2:mem:flush-" + DATABASES.incrementAndGet());
        try (Statement st = real.createStatement()) {
            st.execute("create table customer (id bigint primary key, name varchar(60), tier varchar(20),"
                    + " tags varchar(200))");
            st.execute("insert into customer values (601, 'Priya Raman', 'silver', 'newsletter'),"
                    + " (602, 'Jonas Becker', 'gold', ''), (603, 'Sofia Rossi', 'bronze', 'b2b'),"
                    + " (604, 'Kwame Mensah', 'gold', 'b2b,newsletter')");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        real.close();
    }

    /** A connection that counts the UPDATE statements executed through it. */
    private Connection counting() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {Connection.class}, (p, m, args) -> {
                    Object result = call(m, real, args);
                    if (result instanceof Statement st) {
                        String prepared = args != null && args.length > 0 && args[0] instanceof String s ? s : null;
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {m.getReturnType()},
                                (sp, sm, sargs) -> {
                                    if (sm.getName().startsWith("execute")) {
                                        String sql = sargs != null && sargs.length > 0 && sargs[0] instanceof String s
                                                ? s : prepared;
                                        if (sql != null && sql.trim().toLowerCase(Locale.ROOT).startsWith("update")) {
                                            updates.incrementAndGet();
                                        }
                                    }
                                    return call(sm, st, sargs);
                                });
                    }
                    return result;
                });
    }

    private static Object call(Method m, Object target, Object[] args) throws Throwable {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private String column(String column, long id) throws SQLException {
        try (Statement st = real.createStatement();
             ResultSet rs = st.executeQuery("select " + column + " from customer where id = " + id)) {
            rs.next();
            return rs.getString(1);
        }
    }

    @Test
    void writesARenamedCustomer() throws SQLException {
        CustomerSession session = new CustomerSession(counting());
        session.find(601);
        session.find(602).name = "Jonas Becker-Hale";
        session.find(603);

        session.flush();

        assertThat(column("name", 602)).isEqualTo("Jonas Becker-Hale");
        assertThat(column("name", 601)).isEqualTo("Priya Raman");
    }

    @Test
    void leavesEveryRowAsItWasWhenNothingChanged() throws SQLException {
        CustomerSession session = new CustomerSession(counting());
        session.find(601);
        session.find(604);

        session.flush();

        assertThat(column("tier", 601)).isEqualTo("silver");
        assertThat(column("tags", 604)).isEqualTo("b2b,newsletter");
    }

    @Test
    void writesATagAddedToTheList() throws SQLException {
        CustomerSession session = new CustomerSession(counting());
        session.find(603).tags.add("wholesale");

        session.flush();

        assertThat(column("tags", 603)).isEqualTo("b2b,wholesale");
    }

    @Test
    void writesOnlyTheCustomerThatChanged() throws SQLException {
        CustomerSession session = new CustomerSession(counting());
        session.find(601);
        session.find(602);
        session.find(604).tier = "platinum";

        int written = session.flush();

        assertThat(updates.get()).as("UPDATE statements").isEqualTo(1);
        assertThat(written).isEqualTo(1);
        assertThat(column("tier", 604)).isEqualTo("platinum");
    }

    @Test
    void anEqualValueIsNotAChange() throws SQLException {
        CustomerSession session = new CustomerSession(counting());
        CustomerSession.Customer sofia = session.find(603);
        sofia.tier = new String("bronze");                 // what a form posts back: equal text, a new object
        sofia.name = new StringBuilder("Sofia ").append("Rossi").toString();

        int written = session.flush();

        assertThat(updates.get()).as("UPDATE statements").isZero();
        assertThat(written).isZero();
    }

    @Test
    void aSecondFlushWritesOnlyWhatChangedSinceTheFirst() throws SQLException {
        CustomerSession session = new CustomerSession(counting());
        session.find(601).tier = "gold";
        session.find(602);
        session.flush();
        int afterFirst = updates.get();

        int second = session.flush();
        assertThat(updates.get() - afterFirst).as("UPDATE statements in the second flush").isZero();
        assertThat(second).isZero();

        session.find(602).tier = "silver";
        int third = session.flush();
        assertThat(third).isEqualTo(1);
        assertThat(column("tier", 602)).isEqualTo("silver");
    }
}
