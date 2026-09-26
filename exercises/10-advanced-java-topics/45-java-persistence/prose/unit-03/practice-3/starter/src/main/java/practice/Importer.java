package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Importer {

    private Importer() {
    }

    public record Person(int id, String name, String email) {
    }

    /** Imports every person it can in one transaction; returns the ids imported. */
    public static List<Integer> importAll(Connection connection, List<Person> people) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
