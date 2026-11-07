package practice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TransfersTest {

    /** One fresh in-memory database with four wallets, and an auto-commit connection that watches it. */
    static final class Bank implements AutoCloseable {
        final String url = "jdbc:h2:mem:" + UUID.randomUUID();
        final Connection observer;
        private final List<Connection> opened = new ArrayList<>();

        Bank() throws SQLException {
            observer = DriverManager.getConnection(url);
            try (Statement s = observer.createStatement()) {
                s.execute("CREATE TABLE wallet (id INT PRIMARY KEY, balance INT NOT NULL CHECK (balance BETWEEN 0 AND 1000))");
                s.execute("INSERT INTO wallet VALUES (21, 100), (22, 100), (23, 990), (24, 500)");
            }
        }

        Connection connection() throws SQLException {
            Connection c = DriverManager.getConnection(url);
            opened.add(c);
            try (Statement s = c.createStatement()) {
                s.execute("SET LOCK_TIMEOUT 10000");
            }
            return c;
        }

        int balance(int wallet) throws SQLException {
            try (PreparedStatement p = observer.prepareStatement("SELECT balance FROM wallet WHERE id = ?")) {
                p.setInt(1, wallet);
                try (ResultSet r = p.executeQuery()) {
                    r.next();
                    return r.getInt(1);
                }
            }
        }

        boolean isBlocked(int session) throws SQLException {
            try (PreparedStatement p = observer.prepareStatement(
                    "SELECT BLOCKER_ID FROM INFORMATION_SCHEMA.SESSIONS WHERE SESSION_ID = ?")) {
                p.setInt(1, session);
                try (ResultSet r = p.executeQuery()) {
                    return r.next() && r.getObject(1) != null;
                }
            }
        }

        /** Spins until H2 reports the session waiting for a lock, or the work is done; at most 10 s. */
        void awaitBlockedOrDone(int session, Future<?> work) throws SQLException {
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!work.isDone() && !isBlocked(session) && System.nanoTime() < end) {
                Thread.onSpinWait();
            }
        }

        @Override
        public void close() throws SQLException {
            for (Connection c : opened) c.close();
            observer.close();
        }
    }

    private Bank bank;

    @BeforeEach
    void openDatabase() throws SQLException {
        bank = new Bank();
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        bank.close();
    }

    private static void addOne(Connection c, int wallet) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE wallet SET balance = balance + 1 WHERE id = ?")) {
            p.setInt(1, wallet);
            p.executeUpdate();
        }
    }

    private static int sessionOf(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT SESSION_ID()")) {
            r.next();
            return r.getInt(1);
        }
    }

    private static <T> Future<T> inBackground(Callable<T> work) {
        FutureTask<T> task = new FutureTask<>(work);
        Thread t = new Thread(task, "wallets");
        t.setDaemon(true);
        t.start();
        return task;
    }

    /** The connection, with every SQLException with SQLState 40001 that a statement on it throws noted in saw40001. */
    private static Connection noting40001(Connection c, AtomicBoolean saw40001) {
        return (Connection) Proxy.newProxyInstance(TransfersTest.class.getClassLoader(), new Class<?>[] {Connection.class},
                (proxy, method, args) -> {
                    Object result = invoke(c, method, args, saw40001);
                    if (result instanceof PreparedStatement ps) {
                        return Proxy.newProxyInstance(TransfersTest.class.getClassLoader(), new Class<?>[] {PreparedStatement.class},
                                (p2, m2, a2) -> invoke(ps, m2, a2, saw40001));
                    }
                    if (result instanceof Statement st) {
                        return Proxy.newProxyInstance(TransfersTest.class.getClassLoader(), new Class<?>[] {Statement.class},
                                (p2, m2, a2) -> invoke(st, m2, a2, saw40001));
                    }
                    return result;
                });
    }

    private static Object invoke(Object target, java.lang.reflect.Method m, Object[] args, AtomicBoolean saw40001) throws Throwable {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SQLException s && "40001".equals(s.getSQLState())) saw40001.set(true);
            throw e.getCause();
        }
    }

    /**
     * The interest job adds 1 to wallet 22, then 1 to wallet 21, in one transaction, and runs itself again
     * from the start when it is the deadlock victim.
     */
    private static void interestJob(Connection job, boolean firstAlreadyDone, Callable<Void> beforeSecond) throws Exception {
        for (int attempt = 1; ; attempt++) {
            try {
                if (attempt > 1 || !firstAlreadyDone) addOne(job, 22);
                if (attempt == 1) beforeSecond.call();
                addOne(job, 21);
                job.commit();
                return;
            } catch (SQLException e) {
                job.rollback();
                if (!"40001".equals(e.getSQLState()) || attempt == 5) throw e;
            }
        }
    }

    /** What one crossing ended with. */
    record Crossing(Throwable thrown, boolean transferSaw40001, int wallet21, int wallet22) { }

    /**
     * A transfer of 40 from wallet 21 to wallet 22 crosses the interest job, which wants the same two wallets
     * in the other order. With jobStartsFirst, the job's transaction begins first and the transfer's credit
     * closes the cycle; otherwise the transfer's transaction begins first and the job's second update closes
     * it. H2 must roll one of them back; the job reruns itself when it is the one.
     */
    private Crossing crossing(boolean jobStartsFirst) throws Exception {
        try (Bank b = new Bank()) {
            Connection job = b.connection(), raw = b.connection();
            int tellerSession = sessionOf(raw), jobSession = sessionOf(job);   // read in auto-commit: no transaction left open
            AtomicBoolean saw40001 = new AtomicBoolean();
            Connection teller = noting40001(raw, saw40001);
            job.setAutoCommit(false);
            if (jobStartsFirst) addOne(job, 22);                                 // the job's transaction begins here
            List<Future<Void>> jobRun = new ArrayList<>();
            Transfers transfers = new Transfers(() -> {
                if (!jobRun.isEmpty()) return;
                if (jobStartsFirst) {
                    Future<Void> f = inBackground(() -> {
                        interestJob(job, true, () -> null);
                        return null;
                    });
                    jobRun.add(f);
                    b.awaitBlockedOrDone(jobSession, f);       // the job waits for wallet 21
                } else {
                    CountDownLatch firstDone = new CountDownLatch(1);
                    List<Future<Void>> self = new ArrayList<>();
                    Future<Void> f = inBackground(() -> {
                        interestJob(job, false, () -> {
                            firstDone.countDown();                 // the job holds wallet 22
                            b.awaitBlockedOrDone(tellerSession, self.get(0));
                            return null;
                        });
                        return null;
                    });
                    self.add(f);
                    jobRun.add(f);
                    firstDone.await(10, TimeUnit.SECONDS);
                }
            });
            Future<Void> done = inBackground(() -> {
                transfers.transfer(teller, 21, 22, 40);
                return null;
            });
            Throwable thrown = null;
            try {
                done.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                thrown = e.getCause();
            }
            assertThat(jobRun).as("the transfer never debited through debit(), so the interest job never ran").isNotEmpty();
            jobRun.get(0).get(30, TimeUnit.SECONDS);
            return new Crossing(thrown, saw40001.get(), b.balance(21), b.balance(22));
        }
    }

    /** Both crossings; fails loudly if H2 never rolled back the transfer, since then no edge is tested. */
    private List<Crossing> bothCrossings() throws Exception {
        List<Crossing> both = List.of(crossing(true), crossing(false));
        assertThat(both).as("H2 rolled back the interest job in both crossings and never the transfer, so this test "
                + "checks nothing. It assumes H2 picks its deadlock victim by which transaction began first or by "
                + "which request closed the cycle; the two crossings differ in both.")
                .anyMatch(Crossing::transferSaw40001);
        return both;
    }

    @Test
    void movesMoneyBetweenTwoWallets() throws Exception {
        Connection teller = bank.connection();

        inBackground(() -> {
            new Transfers().transfer(teller, 21, 22, 35);
            return 0;
        }).get(15, TimeUnit.SECONDS);

        assertThat(bank.balance(21)).isEqualTo(65);
        assertThat(bank.balance(22)).isEqualTo(135);
    }

    @Test
    void aTransferPickedAsADeadlockVictimNeitherLosesNorCreatesMoney() throws Exception {
        for (Crossing c : bothCrossings()) {
            assertThat(c.wallet21() + c.wallet22()).as("total of wallets 21 and 22").isEqualTo(202);
        }
    }

    @Test
    void aTransferPickedAsADeadlockVictimStillCompletes() throws Exception {
        for (Crossing c : bothCrossings()) {
            assertThat(c.thrown()).isNull();
            assertThat(c.wallet21()).isEqualTo(61);
            assertThat(c.wallet22()).isEqualTo(141);
        }
    }

    @Test
    void aTransferTheDatabaseRefusesIsNotTriedAgain() throws Exception {
        AtomicInteger debits = new AtomicInteger();
        Transfers transfers = new Transfers(debits::incrementAndGet);
        Connection teller = bank.connection();

        Throwable thrown = catchThrowable(() -> inBackground(() -> {
            transfers.transfer(teller, 24, 23, 30);   // wallet 23 would hold 1020
            return 0;
        }).get(15, TimeUnit.SECONDS));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasCauseInstanceOf(SQLException.class);
        assertThat(debits.get()).as("debits made; the transfer debits through debit()").isEqualTo(1);
        assertThat(bank.balance(24)).isEqualTo(500);
        assertThat(bank.balance(23)).isEqualTo(990);
    }
}
