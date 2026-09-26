package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

public final class Importer {

    private Importer() {
    }

    public record Person(int id, String name, String email) {
    }

    /** Imports every person it can in one transaction; returns the ids imported. */
    public static List<Integer> importAll(Connection connection, List<Person> people) throws SQLException {
        List<Integer> imported = new ArrayList<>();
        connection.setAutoCommit(false);
        try (PreparedStatement person = connection.prepareStatement("INSERT INTO people (id, name) VALUES (?, ?)");
             PreparedStatement email = connection.prepareStatement("INSERT INTO emails (person_id, email) VALUES (?, ?)")) {
            for (Person p : people) {
                person.setInt(1, p.id());
                person.setString(2, p.name());
                person.executeUpdate();
                Savepoint savepoint = connection.setSavepoint();
                try {
                    email.setInt(1, p.id());
                    email.setString(2, p.email());
                    email.executeUpdate();
                    imported.add(p.id());
                } catch (SQLException e) {
                    connection.rollback(savepoint);
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        return imported;
    }
}
