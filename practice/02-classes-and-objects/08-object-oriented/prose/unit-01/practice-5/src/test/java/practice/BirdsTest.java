package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BirdsTest {

    @Test
    void aDuckBothSwimsAndFlies() {
        assertThat(new Birds.Duck("Donald").move()).isEqualTo("Donald swims and Donald flies");
    }

    @Test
    void aPenguinKeepsTheSwimmerDefault() {
        assertThat(new Birds.Penguin("Pingu").move()).isEqualTo("Pingu swims");
    }

    @Test
    void aBatKeepsTheFlyerDefault() {
        assertThat(new Birds.Bat("Bruce").move()).isEqualTo("Bruce flies");
    }
}
