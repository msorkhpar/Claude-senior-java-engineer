package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalsTest {

    @Test
    void aDogBarksThroughAnAnimalReference() {
        Animals.Animal pet = new Animals.Dog("Rex");
        assertThat(pet.makeSound()).isEqualTo("Rex barks: Woof! Woof!");
        assertThat(((Animals.Dog) pet).wagTail()).isEqualTo("Rex is wagging its tail");
        assertThat(new Animals.Animal("Generic").makeSound()).isEqualTo("The animal makes a sound");
    }

    @Test
    void makeAnimalSoundRunsTheSuperclassVersion() {
        assertThat(new Animals.Dog("Rex").makeAnimalSound()).isEqualTo("The animal makes a sound");
    }

    @Test
    void theNameIsKeptByTheSuperclass() {
        Animals.Animal pet = new Animals.Dog("Rex");
        assertThat(pet.getName()).isEqualTo("Rex");
    }
}
