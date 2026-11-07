package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class PaymentsTest {

    /** The request's connection, lent to pay(). */
    private Connection request;
    /** Another session, as a second server would see the data. */
    private Connection other;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=2000";
        request = DriverManager.getConnection(url);
        other = DriverManager.getConnection(url);
        try (Statement st = other.createStatement()) {
            st.execute("CREATE TABLE wallet (id BIGINT PRIMARY KEY, balance DECIMAL(12,2) NOT NULL)");
            st.execute("CREATE TABLE payment (wallet_id BIGINT NOT NULL, amount DECIMAL(12,2) NOT NULL, "
                    + "reference VARCHAR(16) NOT NULL)");
            st.execute("INSERT INTO wallet VALUES (310, 80.00), (311, 1200.50)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        try (Statement st = other.createStatement()) {
            st.execute("SHUTDOWN");
        } catch (SQLException ignored) {
            // already gone
        }
        request.close();
        other.close();
    }

    /** Calls pay() on the request's connection; an SQLException from it reaches the test as it is. */
    private void pay(long wallet, String amount, String reference) {
        assertTimeoutPreemptively(Duration.ofSeconds(20),
                () -> Payments.pay(request, wallet, new BigDecimal(amount), reference));
    }

    private static String balance(Connection c, long wallet) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT balance FROM wallet WHERE id = " + wallet)) {
            rs.next();
            return rs.getBigDecimal(1).toPlainString();
        }
    }

    private static int payments(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM payment")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void aPaymentDebitsTheWalletAndIsRecorded() throws SQLException {
        pay(311, "19.99", "ord-5512");
        assertThat(balance(other, 311)).isEqualTo("1180.51");
        assertThat(payments(other)).isEqualTo(1);
    }

    @Test
    void consecutivePaymentsAllLand() throws SQLException {
        pay(310, "30.00", "ord-5513");
        pay(310, "12.25", "ord-5514");
        pay(311, "0.50", "ord-5515");
        assertThat(balance(other, 310)).isEqualTo("37.75");
        assertThat(balance(other, 311)).isEqualTo("1200.00");
        assertThat(payments(other)).isEqualTo(3);
    }

    @Test
    void aRejectedReferenceLeavesTheWalletAsItWas() throws SQLException {
        assertThrows(SQLException.class, () -> pay(311, "45.00", "invoice-2024-000731"));
        assertThat(balance(other, 311)).isEqualTo("1200.50");
        assertThat(balance(request, 311)).isEqualTo("1200.50");
        assertThat(payments(other)).isZero();
    }

    @Test
    void theRequestsConnectionIsBackInAutoCommitAfterAFailure() throws SQLException {
        assertThrows(SQLException.class, () -> pay(311, "45.00", "invoice-2024-000731"));
        assertThat(request.getAutoCommit()).isTrue();
        try (Statement st = request.createStatement()) {
            st.executeUpdate("INSERT INTO wallet VALUES (312, 5.00)");
        }
        assertThat(balance(other, 312)).isEqualTo("5.00");
    }

    @Test
    void aPaymentFromAWalletThatDoesNotExistIsRefused() throws SQLException {
        assertThrows(SQLException.class, () -> pay(999, "10.00", "ord-5516"));
        assertThat(payments(other)).isZero();
        assertThat(payments(request)).isZero();
    }
}
