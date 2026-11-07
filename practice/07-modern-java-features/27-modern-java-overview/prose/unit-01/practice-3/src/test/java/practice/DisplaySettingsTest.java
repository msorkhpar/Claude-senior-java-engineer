package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DisplaySettingsTest {

    /** What session replication does: write the object on one node, read it on another. */
    private static DisplaySettings replicate(DisplaySettings settings) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(settings);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (DisplaySettings) in.readObject();
        }
    }

    @Test
    void aChosenTimeZoneIsRemembered() {
        DisplaySettings settings = new DisplaySettings("jane.doe");

        settings.setTimeZone("Asia/Kolkata");

        assertThat(settings.timeZone()).contains("Asia/Kolkata");
    }

    @Test
    void aChosenTimeZoneSurvivesSessionReplication() throws Exception {
        DisplaySettings settings = new DisplaySettings("jane.doe");
        settings.setTimeZone("America/Halifax");

        DisplaySettings copy = replicate(settings);

        assertThat(copy.user()).isEqualTo("jane.doe");
        assertThat(copy.timeZone()).contains("America/Halifax");
    }

    @Test
    void aUserWhoNeverChoseHasNoTimeZone() {
        DisplaySettings settings = new DisplaySettings("john.roe");

        assertThat(settings.timeZone()).isEmpty();
    }
}
