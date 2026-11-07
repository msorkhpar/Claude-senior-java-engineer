package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutTest {

    /** A refusal the shipping team added in a release after the checkout was written. */
    static class HazardousGoodsException extends Checkout.ShippingException {
        HazardousGoodsException() {
            super("lithium batteries need ground transport");
        }
    }

    @Test
    void aBookedShipmentShowsItsTrackingNumber() {
        Checkout checkout = new Checkout((order, postcode) -> "TRK-" + order);

        assertThat(checkout.placeOrder("ord-3307", "EH1 1YZ")).isEqualTo("Shipped, tracking number TRK-ord-3307");
    }

    @Test
    void noCarrierTodayAsksTheCustomerToWait() {
        Checkout checkout = new Checkout((order, postcode) -> {
            throw new Checkout.NoCarrierException("all carriers full for 2024-11-29");
        });

        assertThat(checkout.placeOrder("ord-3308", "EH1 1YZ")).isEqualTo(Checkout.LATER_ANSWER);
    }

    @Test
    void aRefusalAddedInALaterReleaseAlsoGetsAnAnswer() {
        Checkout checkout = new Checkout((order, postcode) -> {
            throw new HazardousGoodsException();
        });

        assertThat(checkout.placeOrder("ord-3309", "EH1 1YZ")).isEqualTo(Checkout.LATER_ANSWER);
    }

    @Test
    void aBugInShippingIsNotAnsweredAsARefusal() {
        Checkout checkout = new Checkout((order, postcode) -> {
            String zone = null;
            return zone.trim();
        });

        assertThatThrownBy(() -> checkout.placeOrder("ord-3310", "EH1 1YZ"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aRejectedPostcodeAsksTheCustomerToCheckIt() {
        Checkout checkout = new Checkout((order, postcode) -> {
            throw new Checkout.AddressRejectedException(postcode);
        });

        assertThat(checkout.placeOrder("ord-3311", "ZE2 9AA")).isEqualTo(Checkout.ADDRESS_ANSWER);
    }
}
