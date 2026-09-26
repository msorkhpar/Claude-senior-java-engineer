package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SoundsTest {

    @Test
    void anAnimalSpeaks() {
        assertThat(new Sounds.Animal().speak()).isEqualTo("...");
        assertThat(new Sounds.Animal().speak(2)).isEqualTo("... ...");
        assertThat(new Sounds.Animal().speak(0)).isEmpty();
    }

    @Test
    void theOverrideIsChosenAtRunTime() {
        assertThat(new Sounds.Dog().speak()).isEqualTo("Woof");
        Sounds.Animal a = new Sounds.Dog();
        assertThat(a.speak()).isEqualTo("Woof");
    }

    @Test
    void theInheritedOverloadUsesTheOverride() {
        Sounds.Animal a = new Sounds.Dog();
        assertThat(a.speak(3)).isEqualTo("Woof Woof Woof");
        assertThat(new Sounds.Dog().speak(1)).isEqualTo("Woof");
        assertThat(a.speak(0)).isEmpty();
    }
}
