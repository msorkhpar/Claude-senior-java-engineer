package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NullCheckTest {

    static class Profile {
        private final @NullCheck.NonNull String name;
        private final List<@NullCheck.NonNull String> tags;
        private final List<String> notes;
        private final String nickname;

        Profile(String name, List<String> tags, List<String> notes, String nickname) {
            this.name = name;
            this.tags = tags;
            this.notes = notes;
            this.nickname = nickname;
        }
    }

    private static List<String> list(String... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    @Test
    void reportsANullNonNullField() throws Exception {
        assertThat(Profile.class.getDeclaredField("name").getAnnotation(NullCheck.NonNull.class)).isNull();
        assertThat(NullCheck.problems(new Profile("Ada", list("java"), list(), "ada"))).isEmpty();
        assertThat(NullCheck.problems(new Profile(null, list("java"), list(), "ada"))).containsExactly("name is null");
    }

    @Test
    void aNullElementInANonNullListIsReported() {
        assertThat(NullCheck.problems(new Profile("Ada", list("java", null), list(), "ada")))
                .containsExactly("tags has a null element");
    }

    @Test
    void anUnannotatedTypeMayHoldNulls() {
        assertThat(NullCheck.problems(new Profile("Ada", list("java"), list((String) null), null))).isEmpty();
    }
}
