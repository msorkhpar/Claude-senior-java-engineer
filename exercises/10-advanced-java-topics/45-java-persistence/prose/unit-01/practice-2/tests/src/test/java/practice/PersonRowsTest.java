package practice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.PersonRows.Person;

import static org.assertj.core.api.Assertions.assertThat;

class PersonRowsTest {

    private Connection connection;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE people (id INT PRIMARY KEY, name VARCHAR(100), age INT, email VARCHAR(100))");
            statement.execute("INSERT INTO people VALUES (1, 'Ada', 36, 'ada@example.org'), "
                    + "(2, 'Linus', NULL, NULL), (3, 'Baby', 0, NULL)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private Person row(String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            assertThat(rows.next()).isTrue();
            return PersonRows.map(rows);
        }
    }

    @Test
    void mapsEveryColumnOfARow() throws SQLException {
        Person ada = row("SELECT id, name, age, email FROM people WHERE id = 1");

        assertThat(ada).isEqualTo(new Person(1, "Ada", 36, "ada@example.org"));
    }

    @Test
    void aNullAgeIsNull() throws SQLException {
        Person linus = row("SELECT id, name, age, email FROM people WHERE id = 2");

        assertThat(linus).isEqualTo(new Person(2, "Linus", null, null));
    }

    @Test
    void anAgeOfZeroStaysZero() throws SQLException {
        Person baby = row("SELECT id, name, age, email FROM people WHERE id = 3");

        assertThat(baby).isEqualTo(new Person(3, "Baby", 0, null));
    }

    @Test
    void columnsAreFoundByName() throws SQLException {
        Person ada = row("SELECT email, age, name, id FROM people WHERE id = 1");

        assertThat(ada).isEqualTo(new Person(1, "Ada", 36, "ada@example.org"));
    }
}
