package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Moves money between wallets, one transfer per transaction: the debit and the credit commit
 * together or not at all. Other transactions (other transfers, the interest job) change the same
 * wallets at the same time.
 *
 * <p>Table: {@code wallet(id INT PRIMARY KEY, balance INT NOT NULL CHECK (balance BETWEEN 0 AND 1000))}.
 */
public class Transfers {

    /** Runs after every debit. Production passes nothing. */
    public interface Pause {
        void between() throws Exception;
    }

    /** How many times one call runs the transfer before it gives up. */
    static final int ATTEMPTS = 3;

    private final Pause pause;

    public Transfers() {
        this(() -> { });
    }

    public Transfers(Pause pause) {
        this.pause = pause;
    }

    /** Moves the amount from one wallet to the other and commits; throws if it can't be done. */
    public void transfer(Connection c, int from, int to, int amount) throws Exception {
        c.setAutoCommit(false);
        for (int attempt = 1; ; attempt++) {
            try {
                debit(c, from, amount);
                credit(c, to, amount);
                c.commit();
                return;
            } catch (SQLException e) {
                c.rollback();
                if (!"40001".equals(e.getSQLState()) || attempt == ATTEMPTS) throw e;
            }
        }
    }

    void debit(Connection c, int wallet, int amount) throws Exception {
        change(c, wallet, -amount);
        pause.between();
    }

    void credit(Connection c, int wallet, int amount) throws SQLException {
        change(c, wallet, amount);
    }

    private static void change(Connection c, int wallet, int delta) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE wallet SET balance = balance + ? WHERE id = ?")) {
            p.setInt(1, delta);
            p.setInt(2, wallet);
            p.executeUpdate();
        }
    }
}
