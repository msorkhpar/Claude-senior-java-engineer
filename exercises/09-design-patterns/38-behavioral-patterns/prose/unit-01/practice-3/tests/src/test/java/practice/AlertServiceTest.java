package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import practice.AlertService.Channel;

class AlertServiceTest {

    static final class Inbox implements Channel {
        final List<String> received = new ArrayList<>();

        @Override
        public void deliver(String message) {
            received.add(message);
        }
    }

    final Inbox email = new Inbox();
    final Inbox sms = new Inbox();
    final Inbox chat = new Inbox();
    final Map<String, Channel> channels = Map.of("email", email, "sms", sms, "chat", chat);

    @Test
    void theConfiguredChannelGetsTheAlerts() {
        AlertService alerts = new AlertService(channels, Map.of("alerts.channel", "chat", "region", "eu-2"));
        alerts.raise("queue depth 9000");
        alerts.raise("disk 97% on db-3");
        assertThat(chat.received).containsExactly("queue depth 9000", "disk 97% on db-3");
        assertThat(email.received).isEmpty();
        assertThat(sms.received).isEmpty();
    }

    @Test
    void aMisspelledChannelStopsTheServiceFromStarting() {
        assertThatThrownBy(() -> new AlertService(channels, Map.of("alerts.channel", "smss")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMisspelledChannelNeverSendsAlertsSomewhereElse() {
        new AlertService(channels, Map.of("alerts.channel", "sms")).raise("node n-4 rejoined");
        assertThat(sms.received).containsExactly("node n-4 rejoined");
        try {
            AlertService alerts = new AlertService(channels, Map.of("alerts.channel", "Chat"));
            alerts.raise("cert expires in 3 days");
        } catch (RuntimeException refused) {
            // refusing is fine here; this case checks where the alert went
        }
        assertThat(email.received).isEmpty();
        assertThat(sms.received).containsExactly("node n-4 rejoined");
        assertThat(chat.received).isEmpty();
    }

    @Test
    void withNoChannelConfiguredAlertsGoByEmail() {
        AlertService alerts = new AlertService(channels, Map.of("region", "eu-2"));
        alerts.raise("backup 40 min late");
        assertThat(email.received).containsExactly("backup 40 min late");
    }
}
