package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * One unit of work of the account service: it is opened for one request and dropped at the end.
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

    public CustomerSession(Connection connection) {
        this.connection = connection;
    }

    /** The customer with this id, or null if there is none. */
    public Customer find(long id) throws SQLException {
        // TODO: One request often asks for the same customer from several places: the pricing
        //       step, the address check, the profile update. Each must get the same Customer
        //       object, read from the database once per session, so that an edit made in one
        //       place is what every other place sees. Another session must get objects of its
        //       own. load(id) does the reading.
        throw new IllegalStateException("TODO: find");
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
