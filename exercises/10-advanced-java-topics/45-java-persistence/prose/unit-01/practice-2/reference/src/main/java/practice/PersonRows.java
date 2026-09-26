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
        int id = rows.getInt("id");
        String name = rows.getString("name");
        int ageValue = rows.getInt("age");
        Integer age = rows.wasNull() ? null : ageValue;
        String email = rows.getString("email");
        return new Person(id, name, age, email);
    }
}
