package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutablePersonTest {

    @Test
    void keepsItsValuesAndChangesByCopy() {
        ImmutablePerson ada = new ImmutablePerson("Ada", 36, List.of("Countess"));
        ImmutablePerson older = ada.withAge(37);
        assertThat(older).isNotSameAs(ada);
        assertThat(older.getAge()).isEqualTo(37);
        assertThat(older.getName()).isEqualTo("Ada");
        assertThat(older.getNicknames()).containsExactly("Countess");
        assertThat(ada.getAge()).isEqualTo(36);
        assertThat(ada.getName()).isEqualTo("Ada");
        assertThat(ada.getNicknames()).containsExactly("Countess");
    }

    @Test
    void theCallersListCannotReachIn() {
        List<String> given = new ArrayList<>(List.of("Countess"));
        ImmutablePerson ada = new ImmutablePerson("Ada", 36, given);
        given.add("Enchantress of Numbers");
        assertThat(ada.getNicknames()).containsExactly("Countess");
    }

    @Test
    void theReturnedNicknamesAreReadOnly() {
        ImmutablePerson ada = new ImmutablePerson("Ada", 36, new ArrayList<>(List.of("Countess")));
        assertThatThrownBy(() -> ada.getNicknames().add("Enchantress of Numbers"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(ada.getNicknames()).containsExactly("Countess");
    }

    @Test
    void theClassCannotBeSubclassed() {
        ImmutablePerson ada = new ImmutablePerson("Ada", 36, List.of());
        assertThat(ada.getName()).isEqualTo("Ada");
        assertThat(Modifier.isFinal(ImmutablePerson.class.getModifiers())).isTrue();
    }

    @Test
    void everyFieldIsPrivateAndFinal() {
        ImmutablePerson ada = new ImmutablePerson("Ada", 36, List.of());
        assertThat(ada.getAge()).isEqualTo(36);
        Field[] fields = ImmutablePerson.class.getDeclaredFields();
        assertThat(fields).isNotEmpty();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            assertThat(Modifier.isPrivate(modifiers) && Modifier.isFinal(modifiers))
                    .as("field %s is private and final", field.getName())
                    .isTrue();
        }
    }
}
