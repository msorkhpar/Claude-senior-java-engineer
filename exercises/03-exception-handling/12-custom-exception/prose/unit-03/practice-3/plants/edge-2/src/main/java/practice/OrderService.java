package practice;

import java.sql.SQLException;

/** The data layer: loads an order by id. */
interface OrderDao {
    String load(long id) throws SQLException;
}

/** Checked: the service could not look an order up. */
class OrderLookupException extends Exception {

    OrderLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class OrderService {

    private final OrderDao dao;

    public OrderService(OrderDao dao) {
        this.dao = dao;
    }

    /** Returns the order; a SQLException becomes OrderLookupException("Could not load order " + id). */
    public String findOrder(long id) throws OrderLookupException {
        try {
            return dao.load(id);
        } catch (Exception e) {
            throw new OrderLookupException("Could not load order " + id, e);
        }
    }
}
