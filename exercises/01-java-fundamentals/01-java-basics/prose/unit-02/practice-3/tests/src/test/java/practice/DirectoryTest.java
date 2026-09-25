package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryTest {

    @Test
    void readsTheCity() {
        assertThat(Directory.cityOf(new Directory.Person("Ann", new Directory.Address("Oslo")))).isEqualTo("Oslo");
        assertThat(Directory.cityOf(new Directory.Person("Dee", new Directory.Address("Lima")))).isEqualTo("Lima");
    }

    @Test
    void noPersonIsUnknown() {
        assertThat(Directory.cityOf(null)).isEqualTo("Unknown");
    }

    @Test
    void noAddressIsUnknown() {
        assertThat(Directory.cityOf(new Directory.Person("Bo", null))).isEqualTo("Unknown");
    }

    @Test
    void noCityIsUnknown() {
        assertThat(Directory.cityOf(new Directory.Person("Cy", new Directory.Address(null)))).isEqualTo("Unknown");
    }
}
