package practice;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ReservationsTest {

    private static Reservations.ReservationFailedException failure(Reservations reservations, String sku, int quantity) {
        return catchThrowableOfType(() -> reservations.reserve(sku, quantity),
                Reservations.ReservationFailedException.class);
    }

    @Test
    void takesTheStockWhenTheStoreAccepts() throws Exception {
        List<String> taken = new ArrayList<>();
        Reservations reservations = new Reservations((sku, quantity) -> taken.add(quantity + " " + sku));

        reservations.reserve("LMP-0412", 7);
        reservations.reserve("CBL-1180", 1);

        assertThat(taken).containsExactly("7 LMP-0412", "1 CBL-1180");
    }

    @Test
    void aStoreFailureReachesTheCallerWithTheLineItFailedOn() {
        Reservations reservations = new Reservations((sku, quantity) -> {
            throw new SQLException("could not serialize access", "40001");
        });

        Reservations.ReservationFailedException e = failure(reservations, "LMP-0412", 7);

        assertThat(e).isNotNull();
        assertThat(e.getSku()).isEqualTo("LMP-0412");
        assertThat(e.getQuantity()).isEqualTo(7);
    }

    @Test
    void theStoresOwnErrorStaysReachable() {
        SQLException fromStore = new SQLException("lock wait timeout exceeded", "HY000");
        Reservations reservations = new Reservations((sku, quantity) -> {
            throw fromStore;
        });

        Reservations.ReservationFailedException e = failure(reservations, "TBL-2093", 12);

        assertThat(e).isNotNull();
        assertThat(e.getCause()).isSameAs(fromStore);
    }

    @Test
    void theLogLineSaysWhichLineFailed() {
        Reservations reservations = new Reservations((sku, quantity) -> {
            throw new SQLException("deadlock detected", "40P01");
        });

        Reservations.ReservationFailedException e = failure(reservations, "TBL-2093", 12);

        assertThat(e).isNotNull();
        assertThat(e.toString()).contains("TBL-2093").contains("12");
    }
}
