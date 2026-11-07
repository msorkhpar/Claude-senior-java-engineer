package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceRepositoryTest {

    private static final AtomicInteger DATABASES = new AtomicInteger();

    private Connection real;
    private final AtomicInteger executed = new AtomicInteger();

    @BeforeEach
    void openDatabase() throws SQLException {
        real = DriverManager.getConnection("jdbc:h2:mem:invoices-" + DATABASES.incrementAndGet());
        try (Statement st = real.createStatement()) {
            st.execute("create table invoice (id bigint primary key, customer varchar(60))");
            st.execute("create table invoice_line (invoice_id bigint references invoice(id), line_no int,"
                    + " sku varchar(20), qty int, primary key (invoice_id, line_no))");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        real.close();
    }

    private void sql(String... statements) throws SQLException {
        try (Statement st = real.createStatement()) {
            for (String s : statements) st.execute(s);
        }
    }

    /** The full overview: one invoice with three lines, one with none, two more. */
    private void overview() throws SQLException {
        sql("insert into invoice values (7004, 'Pinecrest Bakery'), (7001, 'Harbor Tools'),"
                        + " (7003, 'Quarry Lane Dental'), (7002, 'Northgate Florist')",
            "insert into invoice_line values (7001, 3, 'BOLT-M8', 200), (7001, 1, 'DRILL-18V', 2),"
                        + " (7001, 2, 'BIT-SET', 2), (7002, 1, 'VASE-XL', 6),"
                        + " (7004, 2, 'TRAY-60', 12), (7004, 1, 'OVEN-MITT', 4)");
    }

    /** A connection that counts every statement executed through it. */
    private Connection counting() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {Connection.class}, (p, m, args) -> {
                    Object result = call(m, real, args);
                    if (result instanceof Statement st) {
                        Class<?> type = m.getReturnType();
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {type},
                                (sp, sm, sargs) -> {
                                    if (sm.getName().startsWith("execute")) executed.incrementAndGet();
                                    return call(sm, st, sargs);
                                });
                    }
                    return result;
                });
    }

    private static Object call(java.lang.reflect.Method m, Object target, Object[] args) throws Throwable {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static InvoiceRepository.Invoice only(List<InvoiceRepository.Invoice> all, long id) {
        List<InvoiceRepository.Invoice> found = all.stream().filter(i -> i.id == id).toList();
        assertThat(found).as("invoices with id " + id).hasSize(1);
        return found.get(0);
    }

    @Test
    void loadsEachInvoiceWithItsLine() throws SQLException {
        sql("insert into invoice values (7020, 'Copperfield Print'), (7010, 'Elm Street Garage'),"
                        + " (7030, 'Riverside Kennels')",
            "insert into invoice_line values (7010, 1, 'TYRE-205', 4), (7030, 1, 'KIBBLE-15', 3),"
                        + " (7020, 1, 'TONER-K', 9)");

        List<InvoiceRepository.Invoice> all = new InvoiceRepository(counting()).loadAll();

        assertThat(all).extracting(i -> i.id).containsExactly(7010L, 7020L, 7030L);
        assertThat(all).extracting(i -> i.customer)
                .containsExactly("Elm Street Garage", "Copperfield Print", "Riverside Kennels");
        assertThat(all.get(1).lines).containsExactly(new InvoiceRepository.Line("TONER-K", 9));
    }

    @Test
    void sendsOneStatementForTheWholeOverview() throws SQLException {
        overview();

        new InvoiceRepository(counting()).loadAll();

        assertThat(executed.get()).as("statements executed").isEqualTo(1);
    }

    @Test
    void keepsAnInvoiceThatHasNoLinesYet() throws SQLException {
        overview();

        List<InvoiceRepository.Invoice> all = new InvoiceRepository(counting()).loadAll();

        assertThat(all).extracting(i -> i.id).contains(7003L);
    }

    @Test
    void listsAnInvoiceOnceWithAllItsLinesInOrder() throws SQLException {
        overview();

        List<InvoiceRepository.Invoice> all = new InvoiceRepository(counting()).loadAll();

        assertThat(only(all, 7001).lines).containsExactly(new InvoiceRepository.Line("DRILL-18V", 2),
                new InvoiceRepository.Line("BIT-SET", 2), new InvoiceRepository.Line("BOLT-M8", 200));
        assertThat(all).extracting(i -> i.id).containsExactly(7001L, 7002L, 7003L, 7004L);
    }

    @Test
    void anInvoiceWithoutLinesHasNoLines() throws SQLException {
        overview();

        List<InvoiceRepository.Invoice> all = new InvoiceRepository(counting()).loadAll();

        assertThat(only(all, 7003).lines).isEmpty();
    }
}
