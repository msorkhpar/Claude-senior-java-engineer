package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.UnitOfWork.Product;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfWorkTest {

    private Connection connection;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), price DECIMAL(10,2))");
            statement.execute("INSERT INTO products VALUES (1, 'Pen', 1.50), (2, 'Ink', 4.00)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private BigDecimal storedPrice(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT price FROM products WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getBigDecimal(1);
            }
        }
    }

    @Test
    void flushWritesAChangedProduct() throws SQLException {
        UnitOfWork work = new UnitOfWork(connection);
        Product pen = work.load(1);

        pen.setPrice(new BigDecimal("2.25"));

        assertThat(work.flush()).isEqualTo(1);
        assertThat(storedPrice(1)).isEqualByComparingTo("2.25");
    }

    @Test
    void anUntouchedProductIsNotWritten() throws SQLException {
        UnitOfWork work = new UnitOfWork(connection);
        Product pen = work.load(1);
        work.load(2);

        pen.setPrice(new BigDecimal("2.25"));
        try (Statement other = connection.createStatement()) {
            other.executeUpdate("UPDATE products SET price = 5.00 WHERE id = 2");
        }

        assertThat(work.flush()).isEqualTo(1);
        assertThat(storedPrice(2)).isEqualByComparingTo("5.00");
    }

    @Test
    void anEqualNameIsNoChange() throws SQLException {
        UnitOfWork work = new UnitOfWork(connection);
        Product pen = work.load(1);

        pen.setName(new StringBuilder("P").append("en").toString());

        assertThat(work.flush()).isZero();
    }

    @Test
    void anEqualPriceWithAnotherScaleIsNoChange() throws SQLException {
        UnitOfWork work = new UnitOfWork(connection);
        Product pen = work.load(1);

        pen.setPrice(new BigDecimal("1.5"));

        assertThat(work.flush()).isZero();
    }

    @Test
    void aSecondFlushWritesNothing() throws SQLException {
        UnitOfWork work = new UnitOfWork(connection);
        Product pen = work.load(1);
        pen.setPrice(new BigDecimal("2.25"));
        work.flush();

        assertThat(work.flush()).isZero();
    }
}
