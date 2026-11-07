package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RowMapperTest {

    record Shipment(String trackingId, int parcels, String city) {
        Shipment {
            if (parcels < 1) {
                throw new IllegalArgumentException("parcels must be at least 1, was " + parcels);
            }
        }
    }

    record Sensor(String sensorId, double celsius, boolean outdoor) {
    }

    /** A row whose columns come in the order given: name, value, name, value, ... */
    private static Map<String, Object> row(Object... namesAndValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            row.put((String) namesAndValues[i], namesAndValues[i + 1]);
        }
        return row;
    }

    @Test
    void buildsARecordFromARowWhoseColumnsFollowTheHeader() {
        Shipment shipment = RowMapper.map(Shipment.class,
                row("trackingId", "TRK-5531", "parcels", 4, "city", "Porto"));
        Sensor sensor = RowMapper.map(Sensor.class,
                row("sensorId", "greenhouse-2", "celsius", 17.5, "outdoor", false));

        assertThat(shipment).isEqualTo(new Shipment("TRK-5531", 4, "Porto"));
        assertThat(sensor).isEqualTo(new Sensor("greenhouse-2", 17.5, false));
    }

    @Test
    void aRowTheRecordRefusesIsStillRefused() {
        Throwable thrown = catchThrowable(() -> RowMapper.map(Shipment.class,
                row("trackingId", "TRK-0904", "parcels", 0, "city", "Gdansk")));

        assertThat(thrown).as("mapping a row with 0 parcels").isNotNull();
        boolean refusedByTheRecord = false;
        for (Throwable t = thrown; t != null; t = t.getCause()) {
            if (t instanceof IllegalArgumentException
                    && String.valueOf(t.getMessage()).contains("parcels must be at least 1")) {
                refusedByTheRecord = true;
            }
        }
        assertThat(refusedByTheRecord)
                .as("the record's own check refused the row; got %s", thrown)
                .isTrue();
    }

    @Test
    void columnsInAnotherOrderStillLandInTheRightComponents() {
        Shipment shipment = RowMapper.map(Shipment.class,
                row("city", "Ghent", "parcels", 2, "trackingId", "TRK-7120"));
        Sensor sensor = RowMapper.map(Sensor.class,
                row("outdoor", true, "sensorId", "roof-north", "celsius", -3.25));

        assertThat(shipment).isEqualTo(new Shipment("TRK-7120", 2, "Ghent"));
        assertThat(sensor).isEqualTo(new Sensor("roof-north", -3.25, true));
    }

    @Test
    void columnsTheRecordDoesNotHaveAreLeftOut() {
        Shipment shipment = RowMapper.map(Shipment.class,
                row("trackingId", "TRK-2268", "parcels", 1, "city", "Tartu",
                        "_sourceLine", 418, "_importedBy", "nightly-import"));

        assertThat(shipment).isEqualTo(new Shipment("TRK-2268", 1, "Tartu"));
    }
}
