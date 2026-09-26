package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaletteTest {

    @Test
    void namesTheShade() {
        assertThat(Palette.shade(Palette.Color.RED)).isEqualTo("Warm");
        assertThat(Palette.shade(Palette.Color.BLUE)).isEqualTo("Cool");
    }

    @Test
    void everyColourHasItsShade() {
        assertThat(Palette.shade(Palette.Color.ORANGE)).isEqualTo("Warm");
        assertThat(Palette.shade(Palette.Color.YELLOW)).isEqualTo("Warm");
        assertThat(Palette.shade(Palette.Color.GREEN)).isEqualTo("Cool");
        assertThat(Palette.shade(Palette.Color.VIOLET)).isEqualTo("Cool");
    }

    @Test
    void nullIsNone() {
        assertThat(Palette.shade(null)).isEqualTo("None");
    }
}
