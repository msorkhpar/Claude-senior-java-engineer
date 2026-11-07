package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PermitsTest {

    @Test
    void supportFindsTheCarFromACodeReadOutAsPrinted() {
        Permits permits = new Permits();
        permits.register("KD730M", "PX4271");
        permits.register("HX219B", "RT8853");
        assertThat(permits.carWithPermit("PX4271")).isEqualTo("KD730M");
        assertThat(permits.carWithPermit("RT8853")).isEqualTo("HX219B");
        assertThat(permits.carWithPermit("ZZ0000")).isNull();

        permits.transfer("KD730M", "LP448S");
        assertThat(permits.carWithPermit("PX4271")).isEqualTo("LP448S");
        assertThat(permits.isPermitted("LP448S")).isTrue();
        assertThat(permits.isPermitted("KD730M")).isFalse();
        assertThatThrownBy(() -> permits.register("HX219B", "MN5502"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void carsWhosePlatesDifferOnlyInLettersThatLookLikeDigitsAreTwoCars() {
        Permits permits = new Permits();
        permits.register("LO-404-D", "PK2201");
        permits.register("L0-404-D", "PK2202");
        permits.register("NI-77-F", "PK2203");
        permits.register("N1-77-F", "PK2204");
        assertThat(permits.carWithPermit("PK2202")).isEqualTo("L0404D");
        assertThat(permits.carWithPermit("PK2203")).isEqualTo("NI77F");
    }

    @Test
    void aCodeReadOutWithSpacesAndLookAlikeLettersFindsItsCar() {
        Permits permits = new Permits();
        permits.register("MV-220-T", "QH7103");
        permits.register("GS-61-W", "WD5180");
        assertThat(permits.carWithPermit("QH 71O3")).isEqualTo("MV220T");
        assertThat(permits.carWithPermit("WD-5I8O")).isEqualTo("GS61W");
    }

    @Test
    void aCodeReadOutInLowerCaseFindsItsCar() {
        Permits permits = new Permits();
        permits.register("CE-93-H", "BT6109");
        assertThat(permits.carWithPermit("bt6io9")).isEqualTo("CE93H");
    }
}
