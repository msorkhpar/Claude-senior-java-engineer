package practice;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConnectorFactoryTest {

    public static class Billing implements ConnectorFactory.Connector {
        public String name() { return "billing"; }
    }

    public static class Shipping implements ConnectorFactory.Connector {
        public String name() { return "shipping"; }
    }

    /** Its database is down. */
    public static class Warehouse implements ConnectorFactory.Connector {
        public Warehouse() throws SQLException {
            throw new SQLException("warehouse-db.example.com:5432 refused the connection");
        }
        public String name() { return "warehouse"; }
    }

    /** Its partner API doesn't answer. */
    public static class Customs implements ConnectorFactory.Connector {
        public Customs() throws TimeoutException {
            throw new TimeoutException("customs.example.org did not answer in 3 s");
        }
        public String name() { return "customs"; }
    }

    /** Has a bug: it reads a setting that has a typo in its own code. */
    public static class Loyalty implements ConnectorFactory.Connector {
        public Loyalty() {
            throw new IllegalArgumentException("unknown setting 'loyalty.tiers.golf'");
        }
        public String name() { return "loyalty"; }
    }

    /** Has a bug that trips an assertion in a library it calls. */
    public static class Returns implements ConnectorFactory.Connector {
        public Returns() {
            throw new AssertionError("refund window must be positive");
        }
        public String name() { return "returns"; }
    }

    /** Needs its endpoint passed in, so it has no no-argument constructor. */
    public static class Tax implements ConnectorFactory.Connector {
        public Tax(String endpoint) { }
        public String name() { return "tax"; }
    }

    @Test
    void startsEveryConnectorWhoseConstructorSucceeds() {
        ConnectorFactory factory = new ConnectorFactory();

        List<ConnectorFactory.Connector> started = factory.createAll(List.of(Billing.class, Shipping.class));

        assertThat(started).extracting(ConnectorFactory.Connector::name).containsExactly("billing", "shipping");
        assertThat(factory.skipped()).isEmpty();
    }

    @Test
    void aClassWithoutANoArgumentConstructorIsAConnectorException() {
        assertThatThrownBy(() -> new ConnectorFactory().create(Tax.class))
                .isInstanceOf(ConnectorFactory.ConnectorException.class);
    }

    @Test
    void anUnreachableConnectorIsSkippedAndTheOthersStillStart() {
        ConnectorFactory factory = new ConnectorFactory();

        List<ConnectorFactory.Connector> started = factory.createAll(
                List.of(Customs.class, Billing.class, Warehouse.class, Shipping.class));

        assertThat(started).extracting(ConnectorFactory.Connector::name).containsExactly("billing", "shipping");
        assertThat(factory.skipped()).hasSize(2);
    }

    @Test
    void theFailureCarriesTheConstructorsOwnException() {
        assertThatThrownBy(() -> new ConnectorFactory().create(Warehouse.class))
                .isInstanceOf(ConnectorFactory.ConnectorException.class)
                .cause().isInstanceOf(SQLException.class)
                .hasMessageContaining("warehouse-db.example.com");
        assertThatThrownBy(() -> new ConnectorFactory().create(Customs.class))
                .isInstanceOf(ConnectorFactory.ConnectorException.class)
                .cause().isInstanceOf(TimeoutException.class);
    }

    @Test
    void aBugInAConstructorReachesTheCallerAsThrown() {
        assertThatThrownBy(() -> new ConnectorFactory().create(Loyalty.class))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("loyalty.tiers.golf");
        assertThatThrownBy(() -> new ConnectorFactory().create(Returns.class))
                .isExactlyInstanceOf(AssertionError.class);
    }
}
