package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketListTest {

    private Connection conn;
    private TicketList list;

    @BeforeEach
    void openTheHelpdeskDatabase() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:");          // private, gone on close
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE ticket(id INT PRIMARY KEY, title VARCHAR(40), priority INT,"
                    + " opened_at TIMESTAMP, customer_tier INT)");
            st.execute("INSERT INTO ticket VALUES"
                    + " (1, 'VPN drops at noon', 2, TIMESTAMP '2026-04-02 09:15:00', 3),"
                    + " (2, 'Printer on floor 3', 4, TIMESTAMP '2026-04-01 16:40:00', 1),"
                    + " (3, 'Expense form crashes', 1, TIMESTAMP '2026-04-03 11:05:00', 2),"
                    + " (4, 'Badge reader offline', 3, TIMESTAMP '2026-03-30 08:20:00', 4)");
        }
        list = new TicketList(conn);
    }

    @AfterEach
    void close() throws SQLException {
        conn.close();
    }

    private int ticketCount() throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ticket")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void sortsByEveryDocumentedKeyInBothDirections() throws SQLException {
        assertThat(list.titles("priority", "asc")).containsExactly(
                "Expense form crashes", "VPN drops at noon", "Badge reader offline", "Printer on floor 3");
        assertThat(list.titles("opened", "desc")).containsExactly(
                "Expense form crashes", "VPN drops at noon", "Printer on floor 3", "Badge reader offline");
        assertThat(list.titles("title", "asc")).containsExactly(
                "Badge reader offline", "Expense form crashes", "Printer on floor 3", "VPN drops at noon");
        assertThat(list.titles("priority", "desc")).containsExactly(
                "Printer on floor 3", "Badge reader offline", "VPN drops at noon", "Expense form crashes");
    }

    @Test
    void aColumnTheApiDoesNotDocumentIsRefused() {
        assertThatThrownBy(() -> list.titles("customer_tier", "asc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aDirectionThatIsNotAscOrDescIsRefused() throws SQLException {
        assertThatThrownBy(() -> list.titles("opened", "asc, customer_tier"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> list.titles("title", "desc; DELETE FROM ticket"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(ticketCount()).isEqualTo(4);
    }
}
