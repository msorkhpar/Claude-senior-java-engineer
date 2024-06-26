package com.github.msorkhpar.claudejavatutor.classobj;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InheritanceExampleTest {

    @Test
    void testAnimalBehavior() {
        InheritanceExample.Animal animal = new InheritanceExample.Animal("Generic Animal");
        assertThat(animal.getName()).isEqualTo("Generic Animal");
        
        // Test makeSound() method
        assertThatCode(() -> animal.makeSound()).doesNotThrowAnyException();
    }

    @Test
    void testDogBehavior() {
        InheritanceExample.Dog dog = new InheritanceExample.Dog("Buddy", "Labrador");
        
        // Test inherited method
        assertThat(dog.getName()).isEqualTo("Buddy");
        
        // Test overridden method
        assertThatCode(() -> dog.makeSound()).doesNotThrowAnyException();
        
        // Test new method in subclass
        assertThatCode(() -> dog.fetch()).doesNotThrowAnyException();
        
        // Test new property in subclass
        assertThat(dog.getBreed()).isEqualTo("Labrador");
    }

    @Test
    void testPolymorphism() {
        InheritanceExample.Animal animal = new InheritanceExample.Dog("Max", "German Shepherd");
        
        // The reference is of type Animal, but the actual object is a Dog
        assertThat(animal.getName()).isEqualTo("Max");
        assertThatCode(() -> animal.makeSound()).doesNotThrowAnyException();
        
        // We can't call dog-specific methods without casting
        assertThatCode(() -> ((InheritanceExample.Dog) animal).fetch()).doesNotThrowAnyException();
    }
}