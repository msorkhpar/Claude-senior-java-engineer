package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.List;
import org.junit.jupiter.api.Test;

class SnapshotTest {

    enum Priority { LOW, URGENT }

    static class Item {
        private final String sku;
        protected int quantity;

        Item(String sku, int quantity) {
            this.sku = sku;
            this.quantity = quantity;
        }
    }

    static class Address {
        private final String city;
        private final String postcode;

        Address(String city, String postcode) {
            this.city = city;
            this.postcode = postcode;
        }
    }

    static class Parcel extends Item {
        static int created = 7;
        private final Address to;
        private final Priority priority;
        private boolean insured = true;

        Parcel(String sku, int quantity, Address to, Priority priority) {
            super(sku, quantity);
            this.to = to;
            this.priority = priority;
        }
    }

    static class Crate {
        private final String label = "C-31";
        private final ArrayDeque<String> handling = new ArrayDeque<>(List.of("fragile", "upright"));
    }

    static class Label {
        private final String text = "Lieferschein";
        private final Charset encoding = StandardCharsets.ISO_8859_1;
    }

    static class Route extends ArrayDeque<String> {
        private final String carrier;

        Route(String carrier, List<String> stops) {
            super(stops);
            this.carrier = carrier;
        }
    }

    @Test
    void writesTheFieldsOfOwnClassesWithTheInheritedOnes() {
        Parcel parcel = new Parcel("SKU-88", 4, new Address("Ghent", "9000"), Priority.URGENT);

        assertThat(Snapshot.render(parcel)).isEqualTo(
                "Parcel{insured=true, priority=URGENT, quantity=4, sku=SKU-88, "
                        + "to=Address{city=Ghent, postcode=9000}}");
    }

    @Test
    void aJdkCollectionHeldInAFieldIsWrittenThroughItsToString() {
        assertThat(Snapshot.render(new Crate()))
                .isEqualTo("Crate{handling=[fragile, upright], label=C-31}");
    }

    @Test
    void aJdkObjectWhoseClassLivesOutsideTheJavaPackagesIsWrittenThroughItsToString() {
        assertThat(StandardCharsets.ISO_8859_1.getClass().getName()).doesNotStartWith("java.");

        assertThat(Snapshot.render(new Label()))
                .isEqualTo("Label{encoding=ISO-8859-1, text=Lieferschein}");
    }

    @Test
    void anObjectWithOneUnreadableFieldIsWrittenWhole() {
        Route route = new Route("Rhenus", List.of("ANR", "DUS", "BSL"));

        assertThat(Snapshot.render(route)).isEqualTo("[ANR, DUS, BSL]");
    }
}
