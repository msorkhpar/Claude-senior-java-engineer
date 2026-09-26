package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MakerTest {

    static class Member {
        final String name;

        public Member() {
            this.name = "default";
        }

        public Member(String name) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("a name is not blank");
            }
            this.name = name;
        }

        public Member(int level) {
            this.name = "level-" + level;
        }

        public Member(java.util.Collection<String> tags) {
            this.name = "tags-" + tags.size();
        }

        private Member(String name, int id) {
            this.name = name + "#" + id;
        }
    }

    @Test
    void makesWithPublicConstructors() throws Exception {
        assertThat(Maker.make(Member.class).name).isEqualTo("default");
        assertThat(Maker.make(Member.class, new String("Alice")).name).isEqualTo("Alice");
        assertThat(Maker.make(Member.class, new java.util.ArrayList<>(java.util.List.of("a", "b"))).name).isEqualTo("tags-2");
    }

    @Test
    void aPrivateConstructorIsUsed() throws Exception {
        assertThat(Maker.make(Member.class, "Bob", 42).name).isEqualTo("Bob#42");
    }

    @Test
    void aBoxedArgumentMatchesAPrimitiveParameter() throws Exception {
        assertThat(Maker.make(Member.class, 7).name).isEqualTo("level-7");
    }

    @Test
    void theConstructorsOwnExceptionIsUnwrapped() {
        assertThatThrownBy(() -> Maker.make(Member.class, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a name is not blank");
    }
}
