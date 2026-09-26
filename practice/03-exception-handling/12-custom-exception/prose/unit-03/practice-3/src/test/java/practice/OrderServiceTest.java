package practice;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class OrderServiceTest {

    @Test
    void findsAnOrder() throws Exception {
        OrderService service = new OrderService(id -> "order-" + id);
        assertThat(service.findOrder(42)).isEqualTo("order-42");
    }

    @Test
    void aStorageFailureIsTranslated() {
        OrderService service = new OrderService(id -> {
            throw new SQLException("connection reset");
        });
        OrderLookupException e = catchThrowableOfType(() -> service.findOrder(42), OrderLookupException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Could not load order 42");
    }

    @Test
    void theSqlExceptionIsKeptAsTheCause() {
        SQLException sql = new SQLException("connection reset");
        OrderService service = new OrderService(id -> {
            throw sql;
        });
        OrderLookupException e = catchThrowableOfType(() -> service.findOrder(7), OrderLookupException.class);
        assertThat(e).isNotNull();
        assertThat(e.getCause()).isSameAs(sql);
    }

    @Test
    void aBugIsNotTranslated() throws Exception {
        assertThat(new OrderService(id -> "order-" + id).findOrder(1)).isEqualTo("order-1");
        NullPointerException bug = new NullPointerException("row mapper");
        OrderService service = new OrderService(id -> {
            throw bug;
        });
        assertThat(catchThrowable(() -> service.findOrder(7))).isSameAs(bug);
    }
}
