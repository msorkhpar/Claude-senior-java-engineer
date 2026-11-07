package practice;

import java.sql.SQLException;

/** The customer service: the layer between the support dashboard and the customer database. */
public class CustomerService {

    public record Customer(String id, String name) {}

    /** The persistence layer, JDBC underneath. Returns null when there is no customer with that id. */
    public interface CustomerRepository {
        Customer load(String id) throws SQLException;
    }

    /** No customer has this id. The dashboard shows "no such customer". */
    public static class UnknownCustomerException extends RuntimeException {
        public UnknownCustomerException(String id) {
            super("unknown customer: " + id);
        }
    }

    /** The customer database failed. The dashboard shows "try again later". */
    public static class CustomerStoreException extends RuntimeException {
        public CustomerStoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    /** The customer with this id. */
    public Customer find(String id) {
        try {
            Customer customer = repository.load(id);
            if (customer == null) throw new UnknownCustomerException(id);
            return customer;
        } catch (Exception e) {
            throw new CustomerStoreException("cannot load customer " + id, e);
        }
    }
}
