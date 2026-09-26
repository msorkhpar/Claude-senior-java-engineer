package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GreetingTest {

    @Test
    void joinsTitleAndName() {
        assertThat(Greeting.address("Dr", "Ada")).isEqualTo("Dr Ada");
        assertThat(Greeting.address("Ms", "Grace")).isEqualTo("Ms Grace");
    }

    @Test
    void aMissingTitleIsLeftOut() {
        assertThat(Greeting.address(null, "Ada")).isEqualTo("Ada");
    }

    @Test
    void aMissingNameIsLeftOut() {
        assertThat(Greeting.address("Dr", null)).isEqualTo("Dr");
        assertThat(Greeting.address(null, null)).isEmpty();
    }
}
