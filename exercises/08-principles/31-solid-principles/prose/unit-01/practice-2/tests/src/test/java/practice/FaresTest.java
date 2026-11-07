package practice;

import org.junit.jupiter.api.Test;

import practice.Fares.FarePolicy;
import practice.Fares.TicketMachine;
import practice.Fares.Trip;
import practice.Fares.WebShop;

import static org.assertj.core.api.Assertions.assertThat;

class FaresTest {

    private final FarePolicy policy = new FarePolicy(10_000);
    private final TicketMachine machine = new TicketMachine(policy);
    private final WebShop web = new WebShop(policy);

    @Test
    void offPeakTripsAreQuotedWhatTheMachineCharges() {
        Trip family = new Trip(2, 2, false, false);   // 36.00
        Trip coach = new Trip(6, 0, true, false);     // 107.00, 10% off

        assertThat(machine.charge(family)).isEqualTo(3_600);
        assertThat(web.quote(family)).isEqualTo(3_600);
        assertThat(machine.charge(coach)).isEqualTo(9_630);
        assertThat(web.quote(coach)).isEqualTo(9_630);
    }

    @Test
    void aPeakGroupAboveTheThresholdGetsBothTheDiscountAndTheSurcharge() {
        Trip school = new Trip(9, 0, false, true);    // 108.00, 10% off, then 20% more

        assertThat(machine.charge(school)).isEqualTo(11_664);
        assertThat(web.quote(school)).isEqualTo(11_664);
    }

    @Test
    void aPeakTripThatOnlyTheSurchargeLiftsOverTheThresholdIsQuotedWhatTheMachineCharges() {
        Trip club = new Trip(7, 1, false, true);      // 90.00, 108.00 with the surcharge

        assertThat(machine.charge(club)).isEqualTo(10_800);
        assertThat(web.quote(club)).isEqualTo(10_800);
    }

    @Test
    void aPolicyWithAnotherThresholdPricesBothChannels() {
        FarePolicy winter = new FarePolicy(6_000);
        Trip trip = new Trip(2, 1, true, false);      // 65.00

        assertThat(new TicketMachine(winter).charge(trip)).isEqualTo(5_850);
        assertThat(new WebShop(winter).quote(trip)).isEqualTo(5_850);
    }
}
