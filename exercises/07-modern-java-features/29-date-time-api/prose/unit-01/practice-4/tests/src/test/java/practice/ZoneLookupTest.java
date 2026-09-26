package practice;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZoneLookupTest {

    @Test
    void findsARegionZone() {
        assertThat(ZoneLookup.zone(String.join("/", "Europe", "Paris"))).isEqualTo(ZoneId.of("Europe/Paris"));
        assertThat(ZoneLookup.zone(String.join("/", "Asia", "Tokyo"))).isEqualTo(ZoneId.of("Asia/Tokyo"));
    }

    @Test
    void anUnknownIdIsRefused() {
        assertThatThrownBy(() -> ZoneLookup.zone("Mars/Olympus_Mons"))
                .as("an unknown ID must not quietly become GMT")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aThreeLetterAbbreviationIsRefused() {
        assertThatThrownBy(() -> ZoneLookup.zone("CST"))
                .as("CST is ambiguous")
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ZoneLookup.zone("IST"))
                .as("IST is ambiguous")
                .isInstanceOf(IllegalArgumentException.class);
    }
}
