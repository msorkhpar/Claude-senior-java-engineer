package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RowBinderTest {

    public record Delivery(String tracking, String depot, LocalDate due) {}

    public record Depot(String code) {
        public Depot {
            code = code.strip().toUpperCase(Locale.ROOT);
        }
    }

    public record Route(String origin, String destination) {}

    public record Gate(String terminal, int number) {}

    public record Weight(String parcel, long grams) {
        public Weight {
            if (grams <= 0) {
                throw new IllegalArgumentException("grams must be positive: " + grams);
            }
        }
    }

    /** A row whose columns come in the order given. */
    private static Map<String, Object> row(Object... nameAndValue) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < nameAndValue.length; i += 2) {
            row.put((String) nameAndValue[i], nameAndValue[i + 1]);
        }
        return row;
    }

    @Test
    void bindsARecordFromItsColumns() throws Exception {
        Delivery d = RowBinder.bind(Delivery.class,
                row("tracking", "TRK-5531", "depot", "LYS-2", "due", LocalDate.of(2031, 2, 14)));

        assertThat(d).isEqualTo(new Delivery("TRK-5531", "LYS-2", LocalDate.of(2031, 2, 14)));
    }

    @Test
    void aBoundRecordWentThroughItsConstructor() throws Exception {
        Depot depot = RowBinder.bind(Depot.class, row("code", "  ber-9 "));

        assertThat(depot.code()).isEqualTo("BER-9");
    }

    @Test
    void columnsMayComeInAnyOrder() throws Exception {
        Route r = RowBinder.bind(Route.class, row("destination", "Porto", "origin", "Gdansk"));

        assertThat(r.origin()).isEqualTo("Gdansk");
        assertThat(r.destination()).isEqualTo("Porto");
    }

    @Test
    void aPrimitiveComponentBindsFromItsBoxedColumn() throws Exception {
        Gate g = RowBinder.bind(Gate.class, row("terminal", "T2", "number", 37));

        assertThat(g).isEqualTo(new Gate("T2", 37));
    }

    @Test
    void aValueTheRecordRefusesReachesTheCallerAsTheRecordsOwnException() {
        assertThatThrownBy(() -> RowBinder.bind(Weight.class, row("parcel", "PX-4", "grams", -250L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("-250");
    }
}
