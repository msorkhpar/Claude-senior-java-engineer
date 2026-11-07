package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PresenceTest {

    @Test
    void aUserIsOnlineWhileAnyOfTheirConnectionsIsOpen() {
        Presence presence = new Presence();
        presence.connect("noor");
        presence.connect("noor");
        presence.connect("tomas");
        presence.connect("noor");

        assertThat(presence.online()).containsExactly("noor", "tomas");
        assertThat(presence.connectionsOf("noor")).isEqualTo(3);

        presence.disconnect("noor");
        presence.disconnect("noor");

        assertThat(presence.online()).containsExactly("noor", "tomas");
        assertThat(presence.connectionsOf("noor")).isEqualTo(1);
    }

    @Test
    void closingTheLastConnectionTakesTheUserOffline() {
        Presence presence = new Presence();
        presence.connect("ines");
        presence.connect("ines");
        presence.connect("kofi");
        presence.disconnect("ines");
        presence.disconnect("ines");

        assertThat(presence.online()).containsExactly("kofi");
        assertThat(presence.connectionsOf("ines")).isZero();

        presence.connect("ines");
        assertThat(presence.online()).containsExactly("ines", "kofi");
        assertThat(presence.connectionsOf("ines")).isEqualTo(1);
    }

    @Test
    void aRepeatedDisconnectChangesNothing() {
        Presence presence = new Presence();
        presence.connect("kofi");
        presence.disconnect("kofi");
        presence.disconnect("kofi");
        presence.disconnect("mira");

        assertThat(presence.online()).isEmpty();
        assertThat(presence.connectionsOf("kofi")).isZero();
        assertThat(presence.connectionsOf("mira")).isZero();

        presence.connect("kofi");
        assertThat(presence.connectionsOf("kofi")).isEqualTo(1);
    }
}
