package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceFormatterTest {

    @AfterEach
    void restoreTheDefault() {
        PriceFormatter.setDefaultDecimals(2);
    }

    @Test
    void formatsWithTheDefaultOrItsOwnDecimals() {
        assertThat(PriceFormatter.defaultDecimals()).isEqualTo(2);
        PriceFormatter usual = new PriceFormatter();
        assertThat(usual.format(1.5)).isEqualTo("1.50");
        assertThat(usual.format(2.346)).isEqualTo("2.35");
        PriceFormatter precise = new PriceFormatter(3);
        assertThat(precise.format(1.5)).isEqualTo("1.500");
    }

    @Test
    void aFormatterWithoutItsOwnFollowsTheDefault() {
        PriceFormatter usual = new PriceFormatter();
        PriceFormatter.setDefaultDecimals(0);
        assertThat(usual.format(7)).isEqualTo("7");
        PriceFormatter.setDefaultDecimals(4);
        assertThat(usual.format(7)).isEqualTo("7.0000");
    }

    @Test
    void anOwnSettingStaysWithItsFormatter() {
        PriceFormatter precise = new PriceFormatter(3);
        PriceFormatter usual = new PriceFormatter();
        assertThat(PriceFormatter.defaultDecimals()).isEqualTo(2);
        assertThat(usual.format(1.5)).isEqualTo("1.50");
        PriceFormatter.setDefaultDecimals(1);
        assertThat(precise.format(1.5)).isEqualTo("1.500");
    }
}
