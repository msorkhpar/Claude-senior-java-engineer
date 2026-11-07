package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepotTest {

    @Test
    void aFreightParcelRefusesWhatNoPalletTakes() {
        Depot.Parcel freight = new Depot.FreightParcel();

        assertThat(freight.accepts(31_500, 90)).isFalse();
        assertThat(freight.accepts(900, 175)).isFalse();
        assertThat(freight.accepts(0, 25)).isFalse();
    }

    @Test
    void aBeltParcelKeepsItsOwnLimitsBesideAFreightParcel() {
        new Depot.FreightParcel();
        Depot.Parcel belt = new Depot.Parcel();

        assertThat(belt.accepts(1_750, 35)).isTrue();
        assertThat(belt.accepts(2_400, 35)).isFalse();
        assertThat(belt.limits()).isEqualTo("up to 2000 g, 40 cm");
    }

    @Test
    void aFreightParcelTakesWhatAPalletTakes() {
        Depot.Parcel freight = new Depot.FreightParcel();

        assertThat(freight.accepts(12_400, 110)).isTrue();
        assertThat(freight.accepts(30_000, 150)).isTrue();
    }

    @Test
    void theIntakeScreenShowsAFreightParcelsLimits() {
        Depot.Parcel freight = new Depot.FreightParcel();

        assertThat(freight.limits()).isEqualTo("up to 30000 g, 150 cm");
    }
}
