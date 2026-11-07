package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One unit of work of the account service: it is opened for one request, flushed, and dropped.
 * Table: customer(id, name, tier, tags), tags stored comma-separated.
 */
public class CustomerSession {

    public static final class Customer {
        public final long id;
        public String name;
        public String tier;
        public final List<String> tags;

        Customer(long id, String name, String tier, List<String> tags) {
            this.id = id;
            this.name = name;
            this.tier = tier;
            this.tags = tags;
        }
    }

    private final Connection connection;
    private final Map<Long, Customer> loaded = new HashMap<>();

    /** A customer's values as they were read, or as they were last written. */
    private record State(String name, String tier, List<String> tags) {}

    private final Map<Long, State> snapshots = new HashMap<>();

    public CustomerSession(Connection connection) {
        this.connection = connection;
    }

    /** The customer with this id, or null if there is none. */
    public Customer find(long id) throws SQLException {
        Customer customer = loaded.get(id);
        if (customer == null) {
            customer = load(id);
            if (customer != null) {
                loaded.put(id, customer);
                remember(customer);
            }
        }
        return customer;
    }

    /** Records the customer's current values as the state the database holds. */
    void remember(Customer customer) {
        snapshots.put(customer.id, new State(customer.name, customer.tier, List.copyOf(customer.tags)));
    }

    /** Whether the customer's values differ from the state the database holds. */
    boolean isDirty(Customer customer) {
        return !snapshots.get(customer.id).equals(new State(customer.name, customer.tier, customer.tags));
    }

    /**
     * Writes to the database every change made to this session's customers since they were
     * read or last written, and returns how many rows it wrote.
     */
    public int flush() throws SQLException {
        int written = 0;
        for (Customer customer : loaded.values()) {
            if (isDirty(customer)) {
                writeRow(customer);
                remember(customer);
                written++;
            }
        }
        return written;
    }

    /** Writes the customer's current values to its row. */
    void writeRow(Customer customer) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "update customer set name = ?, tier = ?, tags = ? where id = ?")) {
            ps.setString(1, customer.name);
            ps.setString(2, customer.tier);
            ps.setString(3, String.join(",", customer.tags));
            ps.setLong(4, customer.id);
            ps.executeUpdate();
        }
    }

    /** Reads the row into a new Customer, or returns null if there is no such row. */
    Customer load(long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("select name, tier, tags from customer where id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String tags = rs.getString(3);
                List<String> list = new ArrayList<>(tags.isEmpty() ? List.of() : Arrays.asList(tags.split(",")));
                return new Customer(id, rs.getString(1), rs.getString(2), list);
            }
        }
    }
}
