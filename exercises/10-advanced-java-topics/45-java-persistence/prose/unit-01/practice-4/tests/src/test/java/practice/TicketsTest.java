package practice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class TicketsTest {

    private Connection agent;
    /** A second agent, who opens tickets of their own. */
    private Connection colleague;
    /** When set, the colleague opens this ticket right after the agent's ticket INSERT runs. */
    private String colleagueOpensMeanwhile;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        agent = DriverManager.getConnection(url);
        colleague = DriverManager.getConnection(url);
        try (Statement st = colleague.createStatement()) {
            st.execute("CREATE TABLE ticket (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, title VARCHAR(80) NOT NULL)");
            st.execute("CREATE TABLE ticket_tag (ticket_id BIGINT NOT NULL REFERENCES ticket (id), tag VARCHAR(20) NOT NULL)");
            st.execute("ALTER TABLE ticket ALTER COLUMN id RESTART WITH 7001");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        try (Statement st = colleague.createStatement()) {
            st.execute("SHUTDOWN");
        } catch (SQLException ignored) {
            // already gone
        }
        agent.close();
        colleague.close();
    }

    private long open(String title, String... tags) {
        return assertTimeoutPreemptively(Duration.ofSeconds(20),
                () -> Tickets.open(watched(), title, List.of(tags)));
    }

    private void colleagueOpens(String title) throws SQLException {
        try (PreparedStatement ps = colleague.prepareStatement("INSERT INTO ticket (title) VALUES (?)")) {
            ps.setString(1, title);
            ps.executeUpdate();
        }
    }

    private String title(long id) throws SQLException {
        try (PreparedStatement ps = colleague.prepareStatement("SELECT title FROM ticket WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private List<String> tags(long id) throws SQLException {
        List<String> tags = new ArrayList<>();
        try (PreparedStatement ps = colleague.prepareStatement("SELECT tag FROM ticket_tag WHERE ticket_id = ? ORDER BY tag")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tags.add(rs.getString(1));
            }
        }
        return tags;
    }

    @Test
    void returnsTheNewTicketsIdAndFilesItsTags() throws SQLException {
        long id = open("laptop will not boot", "hardware", "urgent");
        assertThat(title(id)).isEqualTo("laptop will not boot");
        assertThat(tags(id)).containsExactly("hardware", "urgent");
    }

    @Test
    void eachTicketGetsItsOwnId() throws SQLException {
        long first = open("printer offline", "printer");
        long second = open("cannot reach the VPN", "network");
        assertThat(second).isNotEqualTo(first);
        assertThat(tags(first)).containsExactly("printer");
        assertThat(tags(second)).containsExactly("network");
    }

    @Test
    void aColleaguesTicketOpenedMeanwhileGetsNoneOfTheTags() throws SQLException {
        colleagueOpensMeanwhile = "monitor flickers";
        long id = open("mailbox full", "mail", "quota");
        assertThat(title(id)).isEqualTo("mailbox full");
        assertThat(tags(id)).containsExactly("mail", "quota");
    }

    @Test
    void aTitleThatWasUsedBeforeStillGetsANewTicket() throws SQLException {
        colleagueOpens("password reset");
        open("badge does not open door 3", "access");
        long id = open("password reset", "account");
        List<Long> titled = idsTitled("password reset");
        assertThat(titled).hasSize(2);
        assertThat(id).isEqualTo(titled.get(1));
        assertThat(tags(titled.get(0))).isEmpty();
        assertThat(tags(id)).containsExactly("account");
    }

    private List<Long> idsTitled(String title) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement ps = colleague.prepareStatement("SELECT id FROM ticket WHERE title = ? ORDER BY id")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong(1));
            }
        }
        return ids;
    }

    private static boolean isTicketInsert(Object sql) {
        return sql instanceof String text
                && Pattern.compile("INSERT\\s+INTO\\s+TICKET\\b", Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    // --- the agent's connection: right after a ticket INSERT runs, the colleague may open one too ---

    private Connection watched() {
        return wrap(Connection.class, agent, false);
    }

    private <T> T wrap(Class<T> type, T target, boolean ticketInsert) {
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {type},
                (self, method, args) -> {
                    Object result;
                    try {
                        result = method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                    boolean runsTicketInsert = ticketInsert
                            || type == Statement.class && args != null && isTicketInsert(args[0]);
                    if (runsTicketInsert && method.getName().startsWith("execute") && colleagueOpensMeanwhile != null) {
                        colleagueOpens(colleagueOpensMeanwhile);
                        colleagueOpensMeanwhile = null;
                    }
                    if (result instanceof PreparedStatement ps) {
                        return wrap(PreparedStatement.class, ps, args != null && isTicketInsert(args[0]));
                    }
                    if (result instanceof Statement st) {
                        return wrap(Statement.class, st, false);
                    }
                    return result;
                });
        return type.cast(proxy);
    }
}
