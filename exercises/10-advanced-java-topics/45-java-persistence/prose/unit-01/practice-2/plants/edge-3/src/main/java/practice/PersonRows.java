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
        int id = rows.getInt(1);
        String name = rows.getString(2);
        int ageValue = rows.getInt(3);
        Integer age = rows.wasNull() ? null : ageValue;
        String email = rows.getString(4);
        return new Person(id, name, age, email);
    }
}
