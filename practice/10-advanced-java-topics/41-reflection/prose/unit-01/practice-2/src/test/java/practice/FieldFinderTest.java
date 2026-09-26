package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldFinderTest {

    interface Tagged {
        String TAG = "tag";
    }

    static class Plain {
        public int x;
        public int y;
    }

    static class Parent {
        public String publicField;
        private String privateField;
    }

    static class Child extends Parent implements Tagged {
        public int childPublic;
        private int childPrivate;
    }

    static class GrandChild extends Child {
        protected long grandProtected;
    }

    @Test
    void listsTheFieldsOfASimpleClass() {
        assertThat(FieldFinder.ownFields(Plain.class)).containsExactly("x", "y");
        assertThat(FieldFinder.publicFields(Plain.class)).containsExactly("x", "y");
        assertThat(FieldFinder.allFields(Plain.class)).containsExactly("x", "y");
    }

    @Test
    void ownFieldsIncludePrivateOnes() {
        assertThat(FieldFinder.ownFields(Child.class)).containsExactly("childPrivate", "childPublic");
    }

    @Test
    void publicFieldsIncludeInheritedOnes() {
        assertThat(FieldFinder.publicFields(GrandChild.class))
                .containsExactly("TAG", "childPublic", "publicField");
    }

    @Test
    void publicFieldsIncludeInterfaceConstants() {
        assertThat(FieldFinder.publicFields(Child.class))
                .containsExactly("TAG", "childPublic", "publicField");
    }

    @Test
    void allFieldsWalkEverySuperclass() {
        assertThat(FieldFinder.allFields(GrandChild.class)).containsExactly(
                "grandProtected", "childPrivate", "childPublic", "privateField", "publicField");
    }
}
