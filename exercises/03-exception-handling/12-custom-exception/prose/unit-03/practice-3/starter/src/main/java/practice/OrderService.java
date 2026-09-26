package practice;

import java.sql.SQLException;

/** The data layer: loads an order by id. */
interface OrderDao {
    String load(long id) throws SQLException;
}

/** Checked: the service could not look an order up. */
class OrderLookupException extends Exception {

    OrderLookupException(String message, Throwable cause) {
        throw new UnsupportedOperationException("write OrderLookupException(String, Throwable)");
    }
}

public class OrderService {

    public OrderService(OrderDao dao) {
    }

    /** Returns the order; a SQLException becomes OrderLookupException("Could not load order " + id). */
    public String findOrder(long id) throws OrderLookupException {
        throw new UnsupportedOperationException("write findOrder");
    }
}
