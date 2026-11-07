package practice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AlertTest {

    @Test
    void listsTheRecipientsInAlphabeticalOrder() {
        Alert alert = Alert.to("Disk full on db-2", "ravi", "mei", "otto");

        assertThat(alert.title()).isEqualTo("Disk full on db-2");
        assertThat(alert.recipients()).containsExactly("mei", "otto", "ravi");
        assertThat(alert.render()).isEqualTo("Disk full on db-2 -> mei, otto, ravi");
        assertThat(Alert.to("Queue backlog").recipients()).isEmpty();
    }

    @Test
    void aRotationThatIsNotSetUpYetPagesNobody() {
        String[] rotation = null;

        Alert alert = Alert.to("Certificate expires in 3 days", rotation);

        assertThat(alert.recipients()).isEmpty();
        assertThat(alert.render()).isEqualTo("Certificate expires in 3 days -> ");
    }

    @Test
    void theCallersRotationKeepsItsOrder() {
        String[] rotation = {"zoe", "kai", "ines"};

        Alert.to("Latency over 2 s", rotation);

        assertThat(rotation).containsExactly("zoe", "kai", "ines");
    }

    @Test
    void reusingTheRotationArrayLaterDoesNotChangeTheAlert() {
        String[] rotation = {"zoe", "kai", "ines"};
        Alert first = Alert.to("Latency over 2 s", rotation);

        rotation[0] = "bram";
        rotation[1] = "lena";
        rotation[2] = "tomas";
        Alert second = Alert.to("Error rate over 5%", rotation);

        assertThat(first.recipients()).containsExactly("ines", "kai", "zoe");
        assertThat(second.recipients()).containsExactly("bram", "lena", "tomas");
    }
}
