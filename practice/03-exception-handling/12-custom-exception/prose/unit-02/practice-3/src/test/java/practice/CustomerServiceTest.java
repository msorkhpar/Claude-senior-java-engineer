package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import practice.CustomerService.Customer;
import practice.CustomerService.CustomerStoreException;
import practice.CustomerService.UnknownCustomerException;

class CustomerServiceTest {

    private static final Map<String, Customer> ROWS = Map.of(
            "C-4471", new Customer("C-4471", "Example Freight Ltd"),
            "C-0093", new Customer("C-0093", "Jane Doe"));

    @Test
    void returnsTheCustomerTheRepositoryLoads() {
        CustomerService service = new CustomerService(ROWS::get);
        assertThat(service.find("C-4471")).isEqualTo(ROWS.get("C-4471"));
        assertThat(service.find("C-0093")).isEqualTo(ROWS.get("C-0093"));
    }

    @Test
    void aFailingDatabaseIsReportedAsAStoreFailure() {
        CustomerService service = new CustomerService(id -> { throw new SQLTimeoutException("statement timed out", "HYT00"); });
        Throwable caught = catchThrowable(() -> service.find("C-4471"));
        assertThat(caught).isInstanceOf(CustomerStoreException.class);
    }

    @Test
    void aStoreFailureCarriesTheDatabaseErrorForTheLogs() {
        SQLException fromDriver = new SQLException("too many connections", "08004");
        CustomerService service = new CustomerService(id -> { throw fromDriver; });
        Throwable caught = catchThrowable(() -> service.find("C-0093"));
        assertThat(caught).isInstanceOf(CustomerStoreException.class);
        assertThat(caught.getCause()).isSameAs(fromDriver);
    }

    @Test
    void anIdWithNoCustomerIsReportedAsUnknown() {
        CustomerService service = new CustomerService(ROWS::get);
        Throwable caught = catchThrowable(() -> service.find("C-9999"));
        assertThat(caught).isInstanceOf(UnknownCustomerException.class);
        assertThat(caught.getMessage()).contains("C-9999");
    }

    @Test
    void aBugInTheRepositoryIsNotReportedAsADatabaseFailure() {
        IllegalStateException bug = new IllegalStateException("row mapper: column 'name' not found");
        CustomerService service = new CustomerService(id -> { throw bug; });
        Throwable caught = catchThrowable(() -> service.find("C-4471"));
        assertThat(caught).isSameAs(bug);
    }
}
