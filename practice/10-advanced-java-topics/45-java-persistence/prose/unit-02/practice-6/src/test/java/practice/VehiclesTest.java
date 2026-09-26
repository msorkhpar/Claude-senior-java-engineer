package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.Vehicles.Car;
import practice.Vehicles.Truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehiclesTest {

    private Connection connection;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        execute("CREATE TABLE vehicles (id BIGINT PRIMARY KEY, vehicle_type VARCHAR(10) NOT NULL, "
                + "manufacturer VARCHAR(100), doors INT, payload DECIMAL(10,2))");
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private long count(String where) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM vehicles WHERE " + where)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    @Test
    void savesAndLoadsCarsAndTrucks() throws SQLException {
        Car volvo = new Car(1, "Volvo", 4);
        Truck scania = new Truck(2, "Scania", new BigDecimal("12000.50"));

        Vehicles.save(connection, scania);
        Vehicles.save(connection, volvo);

        assertThat(Vehicles.findAll(connection)).containsExactly(volvo, scania);
    }

    @Test
    void theOtherTypesColumnIsNull() throws SQLException {
        Vehicles.save(connection, new Car(1, "Volvo", 4));
        Vehicles.save(connection, new Truck(2, "Scania", new BigDecimal("12000.50")));

        assertThat(count("id = 1 AND vehicle_type = 'CAR' AND payload IS NULL")).isEqualTo(1);
        assertThat(count("id = 2 AND vehicle_type = 'TRUCK' AND doors IS NULL")).isEqualTo(1);
    }

    @Test
    void theDiscriminatorDecidesTheType() throws SQLException {
        execute("INSERT INTO vehicles VALUES (3, 'TRUCK', 'MAN', NULL, NULL)");
        execute("INSERT INTO vehicles VALUES (5, 'CAR', 'Fiat', NULL, NULL)");

        assertThat(Vehicles.findAll(connection)).containsExactly(new Truck(3, "MAN", null), new Car(5, "Fiat", null));
    }

    @Test
    void anUnknownDiscriminatorIsRefused() throws SQLException {
        execute("INSERT INTO vehicles VALUES (4, 'BUS', 'Setra', NULL, NULL)");

        assertThatThrownBy(() -> Vehicles.findAll(connection)).isInstanceOf(IllegalStateException.class);
    }
}
