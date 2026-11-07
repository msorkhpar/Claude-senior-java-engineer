package practice;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TransfersTest {

    private String url;
    /** Auto-commit on: sees only what was committed, and asks H2 who waits for whom. */
    private Connection observer;
    private final List<Connection> opened = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        url = "jdbc:h2:mem:" + UUID.randomUUID();
        observer = DriverManager.getConnection(url);
        try (Statement s = observer.createStatement()) {
            s.execute("CREATE TABLE wallet (id INT PRIMARY KEY, balance INT NOT NULL CHECK (balance BETWEEN 0 AND 1000))");
            s.execute("INSERT INTO wallet VALUES (21, 100), (22, 100), (23, 990), (24, 500)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        for (Connection c : opened) c.close();
        observer.close();
    }

    private Connection connection() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        opened.add(c);
        try (Statement s = c.createStatement()) {
            s.execute("SET LOCK_TIMEOUT 10000");
        }
        return c;
    }

    private int balance(int wallet) throws SQLException {
        try (PreparedStatement p = observer.prepareStatement("SELECT balance FROM wallet WHERE id = ?")) {
            p.setInt(1, wallet);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
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

    private boolean isBlocked(int session) throws SQLException {
        try (PreparedStatement p = observer.prepareStatement(
                "SELECT BLOCKER_ID FROM INFORMATION_SCHEMA.SESSIONS WHERE SESSION_ID = ?")) {
            p.setInt(1, session);
            try (ResultSet r = p.executeQuery()) {
                return r.next() && r.getObject(1) != null;
            }
        }
    }

    private static <T> Future<T> inBackground(Callable<T> work) {
        FutureTask<T> task = new FutureTask<>(work);
        Thread t = new Thread(task, "wallets");
        t.setDaemon(true);
        t.start();
        return task;
    }

    /**
     * The interest job adds 1 to wallet 22 and then to wallet 21, in one transaction that began
     * before the transfer. Its second update arrives right after the transfer's debit of wallet 21,
     * so the transfer's credit to wallet 22 closes a cycle, and H2 picks the transfer as the victim.
     * Returns the transfer's outcome: null if it returned normally, else what it threw.
     */
    private Throwable transferCrossedByInterestJob(int amount) throws Exception {
        Connection job = connection(), teller = connection();
        int jobSession = sessionOf(job);
        job.setAutoCommit(false);
        addOne(job, 22);
        List<Future<Integer>> jobDone = new ArrayList<>();
        Transfers transfers = new Transfers(() -> {
            if (!jobDone.isEmpty()) return;
            Future<Integer> f = inBackground(() -> {
                addOne(job, 21);
                job.commit();
                return 1;
            });
            jobDone.add(f);
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!f.isDone() && !isBlocked(jobSession) && System.nanoTime() < end) {
                Thread.onSpinWait();
            }
        });
        Future<Integer> done = inBackground(() -> {
            transfers.transfer(teller, 21, 22, amount);
            return 0;
        });
        Throwable outcome = null;
        try {
            done.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            outcome = e.getCause();
        }
        jobDone.get(0).get(30, TimeUnit.SECONDS);
        return outcome;
    }

    @Test
    void movesMoneyBetweenTwoWallets() throws Exception {
        Connection teller = connection();

        inBackground(() -> {
            new Transfers().transfer(teller, 21, 22, 35);
            return 0;
        }).get(15, TimeUnit.SECONDS);

        assertThat(balance(21)).isEqualTo(65);
        assertThat(balance(22)).isEqualTo(135);
    }

    @Test
    void aTransferPickedAsADeadlockVictimNeitherLosesNorCreatesMoney() throws Exception {
        transferCrossedByInterestJob(40);

        assertThat(balance(21) + balance(22)).isEqualTo(202);
    }

    @Test
    void aTransferPickedAsADeadlockVictimStillCompletes() throws Exception {
        Throwable outcome = transferCrossedByInterestJob(40);

        assertThat(outcome).isNull();
        assertThat(balance(21)).isEqualTo(61);
        assertThat(balance(22)).isEqualTo(141);
    }

    @Test
    void aTransferTheDatabaseRefusesIsNotTriedAgain() throws Exception {
        AtomicInteger debits = new AtomicInteger();
        Transfers transfers = new Transfers(debits::incrementAndGet);
        Connection teller = connection();

        Throwable thrown = catchThrowable(() -> inBackground(() -> {
            transfers.transfer(teller, 24, 23, 30);   // wallet 23 would hold 1020
            return 0;
        }).get(15, TimeUnit.SECONDS));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasCauseInstanceOf(SQLException.class);
        assertThat(debits.get()).isEqualTo(1);
        assertThat(balance(24)).isEqualTo(500);
        assertThat(balance(23)).isEqualTo(990);
    }
}
