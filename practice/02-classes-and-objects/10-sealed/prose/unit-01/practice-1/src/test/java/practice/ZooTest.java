package practice;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZooTest {

    @Test
    void eachAnimalMakesItsSound() {
        assertThat(Zoo.sound(new Zoo.Dog())).isEqualTo("Woof");
        assertThat(Zoo.sound(new Zoo.Cat())).isEqualTo("Meow");
        assertThat(Zoo.sound(new Zoo.Bird())).isEqualTo("Tweet");
    }

    @Test
    void animalPermitsExactlyDogCatAndBird() {
        assertThat(Zoo.Animal.class.isSealed()).isTrue();
        assertThat(Zoo.Animal.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(Zoo.Dog.class, Zoo.Cat.class, Zoo.Bird.class);
    }

    @Test
    void dogAndCatEndTheHierarchy() {
        assertThat(Modifier.isFinal(Zoo.Dog.class.getModifiers())).isTrue();
        assertThat(Modifier.isFinal(Zoo.Cat.class.getModifiers())).isTrue();
    }

    @Test
    void birdStaysOpenForParrot() {
        assertThat(Zoo.Animal.class.isSealed()).isTrue();
        assertThat(Modifier.isFinal(Zoo.Bird.class.getModifiers())).isFalse();
        assertThat(Zoo.Bird.class.isSealed()).isFalse();
        assertThat(Zoo.Parrot.class.getSuperclass()).isEqualTo(Zoo.Bird.class);
    }

    @Test
    void aParrotSaysSquawk() {
        assertThat(Zoo.sound(new Zoo.Parrot())).isEqualTo("Squawk");
    }
}
