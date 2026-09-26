package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalTest {

    @Test
    void aDogInheritsItsNameAndAddsItsOwnMembers() {
        Animal generic = new Animal("Generic Animal");
        assertThat(generic.getName()).isEqualTo("Generic Animal");
        assertThat(generic.makeSound()).isEqualTo("The animal makes a sound");
        assertThat(generic.describe()).isEqualTo("Generic Animal the animal");
        Animal.Dog buddy = new Animal.Dog("Buddy", "Labrador");
        assertThat(buddy.getName()).isEqualTo("Buddy");
        assertThat(buddy.getBreed()).isEqualTo("Labrador");
        assertThat(buddy.fetch()).isEqualTo("Buddy is fetching");
    }

    @Test
    void aDogBarksThroughAnAnimalReference() {
        Animal max = new Animal.Dog("Max", "German Shepherd");
        assertThat(max.makeSound()).isEqualTo("The dog barks");
        assertThat(((Animal.Dog) max).fetch()).isEqualTo("Max is fetching");
    }

    @Test
    void aDogsDescriptionBuildsOnTheAnimals() {
        Animal buddy = new Animal.Dog("Buddy", "Labrador");
        assertThat(buddy.describe()).isEqualTo("Buddy the animal (Labrador)");
    }
}
