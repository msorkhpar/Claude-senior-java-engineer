package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class FieldDumpTest {

    /** One of the service's own objects. */
    static final class Shipment {
        private final String trackingId;
        private final int parcels;
        private final List<String> depots;
        private String courierNote;

        Shipment(String trackingId, int parcels, List<String> depots) {
            this.trackingId = trackingId;
            this.parcels = parcels;
            this.depots = depots;
        }
    }

    @Test
    void dumpsThePrivateFieldsOfTheServicesOwnObjects() {
        Shipment shipment = new Shipment("TRK-5531", 3, new ArrayList<>(List.of("Leeds", "Hull")));

        Map<String, String> dump = FieldDump.dump(shipment);

        assertThat(dump).containsExactlyInAnyOrderEntriesOf(Map.of(
                "trackingId", "TRK-5531",
                "parcels", "3",
                "depots", "[Leeds, Hull]",
                "courierNote", "null"));
    }

    @Test
    void aJdkObjectsPrivateFieldsShowAsHiddenInsteadOfFailingTheDump() {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(4);
        queue.add("job-81");

        AtomicReference<Map<String, String>> dump = new AtomicReference<>();
        assertThatCode(() -> dump.set(FieldDump.dump(queue))).doesNotThrowAnyException();

        assertThat(dump.get()).isNotEmpty();
        assertThat(dump.get().values()).containsOnly(FieldDump.HIDDEN);
    }

    @Test
    void aJdkObjectsPublicFieldsAreStillRead() throws IOException {
        StreamTokenizer tokens = new StreamTokenizer(new StringReader("617 parcels"));
        tokens.nextToken();

        Map<String, String> dump;
        try {
            dump = FieldDump.dump(tokens);
        } catch (RuntimeException e) {
            dump = Map.of();
        }

        assertThat(dump).containsEntry("nval", "617.0")
                .containsEntry("ttype", String.valueOf(StreamTokenizer.TT_NUMBER))
                .containsEntry("sval", "null");
        for (Field field : StreamTokenizer.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isPublic(modifiers)) {
                assertThat(dump).containsEntry(field.getName(), FieldDump.HIDDEN);
            }
        }
    }
}
