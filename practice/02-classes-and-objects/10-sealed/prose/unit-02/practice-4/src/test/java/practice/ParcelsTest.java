package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelsTest {

    @Test
    void ratesEachKindOfParcel() {
        assertThat(Parcels.rate(new Parcels.Letter(15))).isEqualTo(1);
        assertThat(Parcels.rate(new Parcels.Letter(50))).isEqualTo(2);
        assertThat(Parcels.rate(new Parcels.Box(5))).isEqualTo(15);
        assertThat(Parcels.rate(new Parcels.Tube(90))).isEqualTo(8);
    }

    @Test
    void aTwentyGramLetterIsStandard() {
        assertThat(Parcels.rate(new Parcels.Letter(20))).isEqualTo(1);
        assertThat(Parcels.rate(new Parcels.Letter(21))).isEqualTo(2);
    }

    @Test
    void aHeavyBoxGoesAsFreight() {
        assertThat(Parcels.rate(new Parcels.Box(30))).isEqualTo(40);
        assertThat(Parcels.rate(new Parcels.Box(31))).isEqualTo(50);
        assertThat(Parcels.rate(new Parcels.Box(80))).isEqualTo(50);
    }
}
