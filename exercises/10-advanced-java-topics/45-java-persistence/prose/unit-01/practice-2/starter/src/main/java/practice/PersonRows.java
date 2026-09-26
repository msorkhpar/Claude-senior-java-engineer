package practice;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class PersonRows {

    private PersonRows() {
    }

    public record Person(int id, String name, Integer age, String email) {
    }

    /** Maps the current row of {@code rows} (next() was already called). */
    public static Person map(ResultSet rows) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
