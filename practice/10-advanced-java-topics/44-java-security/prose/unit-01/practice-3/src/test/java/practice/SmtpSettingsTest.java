package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmtpSettingsTest {

    /** A delivery job that carries the settings it used; its toString is the generated one. */
    record DeliveryJob(String recipient, SmtpSettings relay) {}

    private static final SmtpSettings RELAY =
            new SmtpSettings("relay.test.invalid", 587, "notify-bot", "plum-orbit-sample");

    @Test
    void theSettingsShowHostPortAndUser() {
        assertThat(RELAY.toString()).contains("relay.test.invalid", "587", "notify-bot");
    }

    @Test
    void aJobCarryingTheSettingsShowsWhichRelayItUsed() {
        String job = new DeliveryJob("someone@example.com", RELAY).toString();

        assertThat(job).contains("someone@example.com", "relay.test.invalid", "notify-bot");
    }

    @Test
    void thePasswordDoesNotAppearInTheSettingsOrInAJob() {
        assertThat(RELAY.toString()).doesNotContain("plum-orbit-sample");
        assertThat(new DeliveryJob("someone@example.com", RELAY).toString()).doesNotContain("plum-orbit-sample");
    }

    @Test
    void noPieceOfThePasswordAppears() {
        String shown = RELAY.toString();
        String password = RELAY.password();
        for (int i = 0; i + 3 <= password.length(); i++) {
            assertThat(shown).as("piece \"%s\" of the password", password.substring(i, i + 3))
                    .doesNotContain(password.substring(i, i + 3));
        }
    }
}
