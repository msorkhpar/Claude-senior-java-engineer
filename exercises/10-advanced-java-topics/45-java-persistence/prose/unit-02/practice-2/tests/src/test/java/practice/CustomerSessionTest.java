package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerSessionTest {

    private static final AtomicInteger DATABASES = new AtomicInteger();

    private Connection real;
    private final AtomicInteger executed = new AtomicInteger();

    @BeforeEach
    void openDatabase() throws SQLException {
        real = DriverManager.getConnection("jdbc:h2:mem:customers-" + DATABASES.incrementAndGet());
        try (Statement st = real.createStatement()) {
            st.execute("create table customer (id bigint primary key, name varchar(60), tier varchar(20),"
                    + " tags varchar(200))");
            // each test reads its own ids, so nothing one test loads can answer for another
            st.execute("insert into customer values (501, 'Ada Lindqvist', 'silver', 'newsletter'),"
                    + " (502, 'Omar Haddad', 'gold', ''), (511, 'Mei Tanaka', 'bronze', 'b2b'),"
                    + " (521, 'Luis Ortega', 'gold', 'b2b,newsletter'), (531, 'Ines Moreau', 'silver', '')");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        real.close();
    }

    /** A connection that counts every statement executed through it. */
    private Connection counting() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {Connection.class}, (p, m, args) -> {
                    Object result = call(m, real, args);
                    if (result instanceof Statement st) {
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {m.getReturnType()},
                                (sp, sm, sargs) -> {
                                    if (sm.getName().startsWith("execute")) executed.incrementAndGet();
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

    @Test
    void findsACustomerByIdAndNullForAnUnknownOne() throws SQLException {
        CustomerSession session = new CustomerSession(counting());

        CustomerSession.Customer ada = session.find(501);

        assertThat(ada.name).isEqualTo("Ada Lindqvist");
        assertThat(ada.tier).isEqualTo("silver");
        assertThat(ada.tags).containsExactly("newsletter");
        assertThat(session.find(502).tags).isEmpty();
        assertThat(session.find(999)).isNull();
    }

    @Test
    void findingTheSameIdTwiceGivesTheSameObject() throws SQLException {
        CustomerSession session = new CustomerSession(counting());

        CustomerSession.Customer first = session.find(511);
        first.tier = "silver";
        CustomerSession.Customer second = session.find(511);

        assertThat(second).isSameAs(first);
        assertThat(second.tier).isEqualTo("silver");
    }

    @Test
    void theSecondFindOfAnIdSendsNoStatement() throws SQLException {
        CustomerSession session = new CustomerSession(counting());

        session.find(521);
        int afterFirst = executed.get();
        session.find(521);

        assertThat(executed.get()).as("statements after the second find").isEqualTo(afterFirst);
    }

    @Test
    void twoSessionsGetTwoObjectsForTheSameRow() throws SQLException {
        CustomerSession.Customer inOne = new CustomerSession(counting()).find(531);
        inOne.name = "edited in session one";
        CustomerSession.Customer inTwo = new CustomerSession(counting()).find(531);

        assertThat(inTwo).isNotSameAs(inOne);
        assertThat(inTwo.name).isEqualTo("Ines Moreau");
    }
}
