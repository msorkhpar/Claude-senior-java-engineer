package practice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.Importer.Person;

import static org.assertj.core.api.Assertions.assertThat;

class ImporterTest {

    private Connection connection;
    /** A second connection: it sees only what was committed. */
    private Connection observer;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        connection = DriverManager.getConnection(url);
        observer = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE people (id INT PRIMARY KEY, name VARCHAR(100))");
            statement.execute("CREATE TABLE emails (person_id INT PRIMARY KEY, email VARCHAR(100) UNIQUE)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        observer.close();
        connection.close();
    }

    private List<Integer> committed(String table, String column) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Statement statement = observer.createStatement();
             ResultSet rows = statement.executeQuery("SELECT " + column + " FROM " + table + " ORDER BY " + column)) {
            while (rows.next()) {
                ids.add(rows.getInt(1));
            }
        }
        return ids;
    }

    private static List<Person> withDuplicateEmail() {
        return List.of(new Person(1, "Ada", "ada@example.org"),
                new Person(2, "Linus", String.join("@", "ada", "example.org")),
                new Person(3, "Grace", "grace@example.org"));
    }

    @Test
    void importsEveryPersonInOneCommit() throws SQLException {
        List<Person> people = List.of(
                new Person(1, "Ada", "ada@example.org"),
                new Person(2, "Linus", "linus@example.org"),
                new Person(3, "Grace", "grace@example.org"));
        List<List<Integer>> seenMidway = new ArrayList<>();
        List<Person> watched = new AbstractList<>() {
            @Override
            public Person get(int index) {
                if (index == 2) {
                    try {
                        seenMidway.add(committed("people", "id"));
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return people.get(index);
            }

            @Override
            public int size() {
                return people.size();
            }
        };

        List<Integer> imported = Importer.importAll(connection, watched);

        assertThat(seenMidway).as("rows another connection saw before the last person").containsExactly(List.of());
        assertThat(imported).containsExactly(1, 2, 3);
        assertThat(committed("people", "id")).containsExactly(1, 2, 3);
        assertThat(committed("emails", "person_id")).containsExactly(1, 2, 3);
    }

    @Test
    void aFailedPersonKeepsTheEarlierOnes() throws SQLException {
        List<Integer> imported = Importer.importAll(connection, withDuplicateEmail());

        assertThat(imported).containsExactly(1, 3);
        assertThat(committed("emails", "person_id")).containsExactly(1, 3);
    }

    @Test
    void aFailedPersonLeavesNoHalfOfItself() throws SQLException {
        Importer.importAll(connection, withDuplicateEmail());

        assertThat(committed("people", "id")).containsExactly(1, 3);
    }

    @Test
    void autoCommitIsRestored() throws SQLException {
        List<Integer> imported = Importer.importAll(connection, withDuplicateEmail());

        assertThat(imported).containsExactly(1, 3);
        assertThat(connection.getAutoCommit()).isTrue();
    }
}
