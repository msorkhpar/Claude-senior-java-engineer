package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SealedKindsTest {

    sealed interface Pet permits Cat, Dog, Fish {
    }

    record Cat(String name) implements Pet {
    }

    static non-sealed class Dog implements Pet {
    }

    static sealed class Fish implements Pet permits Goldfish {
    }

    static final class Goldfish extends Fish {
    }

    static class Puppy extends Dog {
    }

    abstract static sealed class Shape permits Round {
    }

    static non-sealed class Round extends Shape {
    }

    static class Plain {
    }

    @Test
    void namesEachKindOfType() {
        assertThat(SealedKinds.kindOf(Goldfish.class)).isEqualTo("final");
        assertThat(SealedKinds.kindOf(Cat.class)).isEqualTo("final");
        assertThat(SealedKinds.kindOf(Fish.class)).isEqualTo("sealed");
        assertThat(SealedKinds.kindOf(Pet.class)).isEqualTo("sealed");
        assertThat(SealedKinds.kindOf(Round.class)).isEqualTo("non-sealed");
        assertThat(SealedKinds.kindOf(Plain.class)).isEqualTo("open");
    }

    @Test
    void nonSealedThroughAnInterface() {
        assertThat(SealedKinds.kindOf(Dog.class)).isEqualTo("non-sealed");
    }

    @Test
    void onlyTheDirectParentCounts() {
        assertThat(SealedKinds.kindOf(Puppy.class)).isEqualTo("open");
    }
}
