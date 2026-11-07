package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class PermitsTest {

    @Test
    void permitsFollowTheirCarsWhenEveryoneWritesPlatesAlike() {
        Permits permits = new Permits();
        permits.register("KD730M", "R-5001");
        permits.register("HX219B", "R-5002");
        assertThat(permits.isPermitted("KD730M")).isTrue();
        assertThat(permits.isPermitted("ZZ999Z")).isFalse();

        permits.transfer("KD730M", "LP448S");
        assertThat(permits.isPermitted("LP448S")).isTrue();
        assertThat(permits.isPermitted("KD730M")).isFalse();

        assertThatThrownBy(() -> permits.transfer("LP448S", "HX219B"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> permits.register("HX219B", "R-5003"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theCameraSeesAPermitBoughtUnderAnotherSpelling() {
        Permits permits = new Permits();
        permits.register("kl 482 t", "R-1001");
        assertThat(permits.isPermitted("KL-482-T")).isTrue();
    }

    @Test
    void aPermitCannotMoveOntoACarThatHoldsOneUnderAnotherSpelling() {
        Permits permits = new Permits();
        permits.register("PQ-311-Z", "R-2001");
        permits.register("RS-220-K", "R-2002");
        assertThatThrownBy(() -> permits.transfer("RS-220-K", "pq 311 z"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theCarAPermitLeavesLosesIt() {
        Permits permits = new Permits();
        permits.register("UV-12-C", "R-3001");
        permits.transfer("uv 12 c", "TW-58-AB");
        assertThat(permits.isPermitted("TW58AB")).isTrue();
        assertThat(permits.isPermitted("UV12C")).isFalse();
    }

    @Test
    void aPlateIsTheSameCarOnAServerWithATurkishDefaultLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Permits permits = new Permits();
            permits.register("bi-104-k", "R-4001");
            assertThat(permits.isPermitted("BI104K")).isTrue();
        } finally {
            Locale.setDefault(saved);
        }
    }
}
