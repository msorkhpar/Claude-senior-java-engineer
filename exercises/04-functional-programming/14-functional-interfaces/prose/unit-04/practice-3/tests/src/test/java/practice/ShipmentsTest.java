package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentsTest {

    private static Shipments.Shipment shipment(String ref, String carrier) {
        return new Shipments.Shipment(ref, carrier);
    }

    @Test
    void sendsBackReferencesOfTheWrongShape() {
        List<String> pasted = List.of("LX204518736NL", "LX20451873NL", "lx204518736nl", "CP990011223DE", "4518736");

        assertThat(Shipments.rejected(pasted)).containsExactly("LX20451873NL", "lx204518736nl", "4518736");
    }

    @Test
    void listsTheShipmentsOfOneCarrier() {
        List<Shipments.Shipment> all = List.of(
                shipment("LX204518736NL", "PostNL"), shipment("CP990011223DE", "DHL"),
                shipment("RB111222333SE", "PostNord"), shipment("CP990011224DE", "DHL"));

        assertThat(Shipments.withCarrier(all, "DHL")).extracting(Shipments.Shipment::trackingRef)
                .containsExactly("CP990011223DE", "CP990011224DE");
    }

    @Test
    void sendsBackAReferencePastedWithTextAroundIt() {
        List<String> pasted = List.of("Ref: LX204518736NL", "CP990011223DE.", "CP990011223DE");

        assertThat(Shipments.rejected(pasted)).containsExactly("Ref: LX204518736NL", "CP990011223DE.");
    }

    @Test
    void sendsBackAReferencePastedWithItsLineBreak() {
        List<String> pasted = List.of("RB111222333SE\n", "RB111222333SE");

        assertThat(Shipments.rejected(pasted)).containsExactly("RB111222333SE\n");
    }

    @Test
    void listsTheShipmentsNotHandedOverYet() {
        List<Shipments.Shipment> all = List.of(
                shipment("LX204518736NL", "PostNL"), shipment("UA000000017US", null),
                shipment("CP990011223DE", "DHL"), shipment("UA000000018US", null));

        assertThat(Shipments.withCarrier(all, null)).extracting(Shipments.Shipment::trackingRef)
                .containsExactly("UA000000017US", "UA000000018US");
    }

    @Test
    void listsACarriersShipmentsWhileSomeAreNotHandedOver() {
        List<Shipments.Shipment> all = List.of(
                shipment("UA000000019US", null), shipment("CP990011225DE", "DHL"), shipment("LX204518737NL", "PostNL"));

        assertThat(Shipments.withCarrier(all, "PostNL")).extracting(Shipments.Shipment::trackingRef)
                .containsExactly("LX204518737NL");
    }
}
