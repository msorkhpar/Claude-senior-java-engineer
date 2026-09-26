package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PetsTest {

    @Test
    void eachPetMakesItsOwnSound() {
        Pets.Animal myPet = new Pets.Dog();
        assertThat(myPet.sound()).isEqualTo("The dog barks");
        myPet = new Pets.Cat();
        assertThat(myPet.sound()).isEqualTo("The cat meows");
        assertThat(new Pets.Animal().sound()).isEqualTo("The animal makes a sound");
        assertThat(Pets.chorus(List.of(new Pets.Dog(), new Pets.Cat(), new Pets.Animal())))
                .isEqualTo("The dog barks / The cat meows / The animal makes a sound");
    }

    @Test
    void aPuppyInheritsTheDogsBark() {
        Pets.Animal puppy = new Pets.Puppy();
        assertThat(puppy.sound()).isEqualTo("The dog barks");
    }

    @Test
    void aKittenExtendsTheCatsSound() {
        Pets.Animal kitten = new Pets.Kitten();
        assertThat(kitten.sound()).isEqualTo("The cat meows softly");
    }

    @Test
    void anEmptyChorusIsEmpty() {
        assertThat(Pets.chorus(List.of())).isEmpty();
    }
}
