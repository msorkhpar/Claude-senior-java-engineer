package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Vehicles {

    private Vehicles() {
    }

    public sealed interface Vehicle permits Car, Truck {
        long id();

        String manufacturer();
    }

    public record Car(long id, String manufacturer, Integer doors) implements Vehicle {
    }

    public record Truck(long id, String manufacturer, BigDecimal payload) implements Vehicle {
    }

    /** Inserts the vehicle into the single vehicles table. */
    public static void save(Connection connection, Vehicle vehicle) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Every vehicle, in id order, as the class its vehicle_type names. */
    public static List<Vehicle> findAll(Connection connection) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
