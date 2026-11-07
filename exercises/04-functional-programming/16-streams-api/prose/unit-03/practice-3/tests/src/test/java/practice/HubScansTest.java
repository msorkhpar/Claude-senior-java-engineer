package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HubScansTest {

    private static List<String> loaded(List<String> lines) {
        return HubScans.loadingList(lines).stream().map(Object::toString).toList();
    }

    @Test
    void differentParcelsAreAllLoaded() {
        List<String> lines = List.of("FEDEX;7741;07:00", "DHL;JX5520;07:01", "GLS;88810;07:03");

        assertThat(loaded(lines)).containsExactlyInAnyOrder("FEDEX;7741;07:00", "DHL;JX5520;07:01", "GLS;88810;07:03");
    }

    @Test
    void twoScansOfOneParcelAreEqual() {
        HubScans.Parcel first = HubScans.parse("UPS;1Z77A;06:02");
        HubScans.Parcel second = HubScans.parse("UPS;1Z77A;06:02");

        assertThat(first.equals(second)).isTrue();
        assertThat(first.equals(HubScans.parse("DPD;1Z77A;06:02"))).isFalse();
    }

    @Test
    void aParcelScannedTwiceInTheSameMinuteIsLoadedOnce() {
        List<String> lines = List.of("GLS;40213;06:03", "DPD;0057X;06:03", "GLS;40213;06:03");

        assertThat(loaded(lines)).hasSize(2).contains("GLS;40213;06:03", "DPD;0057X;06:03");
    }

    @Test
    void aParcelScannedAgainLaterIsLoadedOnceAsFirstScanned() {
        List<String> lines = List.of("UPS;1Z77A;06:02", "DHL;JJ900;06:04", "UPS;1Z77A;06:09");

        assertThat(loaded(lines)).hasSize(2).contains("UPS;1Z77A;06:02").doesNotContain("UPS;1Z77A;06:09");
    }

    @Test
    void theListKeepsTheOrderOfFirstScans() {
        List<String> lines = List.of("UPS;1Z77A;06:02", "GLS;40213;06:03", "DPD;0057X;06:05",
                "UPS;1Z77A;06:09", "DHL;JJ900;06:11");

        assertThat(loaded(lines)).containsExactly("UPS;1Z77A;06:02", "GLS;40213;06:03", "DPD;0057X;06:05",
                "DHL;JJ900;06:11");
    }
}
