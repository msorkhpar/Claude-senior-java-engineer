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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeatHoldsTest {

    private String url;
    /** Sees only what was committed, and asks H2 who waits for whom. */
    private Connection observer;
    private final List<Connection> opened = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        url = "jdbc:h2:mem:" + UUID.randomUUID();
        observer = DriverManager.getConnection(url);
        try (Statement s = observer.createStatement()) {
            s.execute("CREATE TABLE shows (id INT PRIMARY KEY, seats_left INT NOT NULL)");
            s.execute("INSERT INTO shows VALUES (41, 6), (42, 3), (43, 1)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        for (Connection c : opened) c.close();
        observer.close();
    }

    private Connection customer(int lockTimeoutMillis) throws SQLException {
        Connection c = DriverManager.getConnection(url);
        opened.add(c);
        try (Statement s = c.createStatement()) {
            s.execute("SET LOCK_TIMEOUT " + lockTimeoutMillis);
        }
        return c;
    }

    private int seatsLeft(int showId) throws SQLException {
        try (PreparedStatement p = observer.prepareStatement("SELECT seats_left FROM shows WHERE id = ?")) {
            p.setInt(1, showId);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private static int sessionOf(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT SESSION_ID()")) {
            r.next();
            return r.getInt(1);
        }
    }

    /** True once H2 reports the session as waiting for a lock. */
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
        Thread t = new Thread(task, "customer");
        t.setDaemon(true);
        t.start();
        return task;
    }

    @Test
    void holdsWhileSeatsLastAndRefusesAfter() throws Exception {
        SeatHolds box = new SeatHolds();
        Connection ana = customer(5000), ben = customer(5000), cai = customer(5000);

        assertThat(inBackground(() -> box.hold(ana, 41, 2)).get(10, TimeUnit.SECONDS)).isTrue();
        assertThat(seatsLeft(41)).isEqualTo(4);
        assertThat(inBackground(() -> box.hold(ben, 41, 4)).get(10, TimeUnit.SECONDS)).isTrue();
        assertThat(seatsLeft(41)).isEqualTo(0);
        assertThat(inBackground(() -> box.hold(cai, 41, 1)).get(10, TimeUnit.SECONDS)).isFalse();
        assertThat(seatsLeft(41)).isEqualTo(0);
    }

    @Test
    void twoCustomersAtOnceNeverGetMoreSeatsThanTheShowHas() throws Exception {
        Connection dee = customer(5000), eli = customer(5000);
        int eliSession = sessionOf(eli);
        List<Future<Boolean>> eliHold = new ArrayList<>();
        // Eli asks for the same seats while Dee is between her read and her write.
        SeatHolds deesBox = new SeatHolds(() -> {
            if (!eliHold.isEmpty()) return;
            Future<Boolean> f = inBackground(() -> new SeatHolds().hold(eli, 42, 2));
            eliHold.add(f);
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!f.isDone() && !isBlocked(eliSession) && System.nanoTime() < end) {
                Thread.onSpinWait();
            }
        });

        boolean deeHeld = inBackground(() -> deesBox.hold(dee, 42, 2)).get(15, TimeUnit.SECONDS);
        boolean eliHeld = eliHold.get(0).get(15, TimeUnit.SECONDS);

        assertThat(List.of(deeHeld, eliHeld)).containsExactlyInAnyOrder(true, false);
        assertThat(seatsLeft(42)).isEqualTo(1);
    }

    @Test
    void aRefusedCustomerDoesNotHoldUpTheNextOne() throws Exception {
        SeatHolds box = new SeatHolds();
        Connection fay = customer(5000), gus = customer(300);

        assertThat(inBackground(() -> box.hold(fay, 43, 2)).get(10, TimeUnit.SECONDS)).isFalse();
        // Fay walks away from the counter; her connection stays open.
        assertThat(inBackground(() -> box.hold(gus, 43, 1)).get(10, TimeUnit.SECONDS)).isTrue();
        assertThat(seatsLeft(43)).isEqualTo(0);
    }
}
