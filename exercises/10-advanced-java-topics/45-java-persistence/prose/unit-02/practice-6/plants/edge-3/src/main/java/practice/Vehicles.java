package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
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
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO vehicles (id, vehicle_type, manufacturer, doors, payload) VALUES (?, ?, ?, ?, ?)")) {
            statement.setLong(1, vehicle.id());
            statement.setString(3, vehicle.manufacturer());
            switch (vehicle) {
                case Car car -> {
                    statement.setString(2, "CAR");
                    statement.setObject(4, car.doors(), Types.INTEGER);
                    statement.setNull(5, Types.DECIMAL);
                }
                case Truck truck -> {
                    statement.setString(2, "TRUCK");
                    statement.setNull(4, Types.INTEGER);
                    statement.setBigDecimal(5, truck.payload());
                }
            }
            statement.executeUpdate();
        }
    }

    /** Every vehicle, in id order, as the class its vehicle_type names. */
    public static List<Vehicle> findAll(Connection connection) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT id, vehicle_type, manufacturer, doors, payload FROM vehicles ORDER BY id")) {
            while (rows.next()) {
                long id = rows.getLong("id");
                String manufacturer = rows.getString("manufacturer");
                String type = rows.getString("vehicle_type");
                vehicles.add(switch (type) {
                    case "CAR" -> new Car(id, manufacturer, rows.getObject("doors", Integer.class));
                    default -> new Truck(id, manufacturer, rows.getBigDecimal("payload"));
                });
            }
        }
        return vehicles;
    }
}
