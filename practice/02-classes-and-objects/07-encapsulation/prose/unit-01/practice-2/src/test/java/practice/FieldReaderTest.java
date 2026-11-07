package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class FieldReaderTest {

    @Test
    void readsThePublicFieldsOfAPlainObject() {
        Map<String, Object> state = FieldReader.read(new Probe());

        assertThat(state).containsOnly(entry("id", "P-9"), entry("readings", 4));
    }

    @Test
    void listsThePrivateFieldsToo() {
        Map<String, Object> state = FieldReader.read(new Invoice());

        assertThat(state).containsOnlyKeys("number", "totalCents");
    }

    @Test
    void showsThePrivateFieldsValues() {
        Map<String, Object> state = FieldReader.read(new Invoice());

        assertThat(state).containsEntry("number", "INV-2207").containsEntry("totalCents", 18_950L);
    }

    @Test
    void includesWhatTheSuperclassesDeclare() {
        Map<String, Object> state = FieldReader.read(new ExpressShipment());

        assertThat(state).containsOnly(entry("hours", 24), entry("carrier", "Hermes"), entry("tracking", "TR-55"));
    }

    @Test
    void aJdkObjectIsShownWithItsValuesHidden() {
        List<String> cart = new ArrayList<>(List.of("tea", "honey"));

        Map<String, Object> state = FieldReader.read(cart);

        assertThat(state).isNotEmpty();
        assertThat(state.values()).allMatch(FieldReader.HIDDEN::equals);
    }
}

class Probe {
    public static final String KIND = "probe";
    public String id = "P-9";
    public int readings = 4;
}

class Invoice {
    private static int issued = 0;
    private final String number = "INV-2207";
    private long totalCents = 18_950;
}

class Shipment {
    private String carrier = "Hermes";
    protected String tracking = "TR-55";
}

class ExpressShipment extends Shipment {
    private int hours = 24;
}
