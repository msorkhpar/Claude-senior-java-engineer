package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class UserDataTest {

    private final UserData.UserDataAdapter adapter = new UserData.UserDataAdapter();

    private static UserData.LegacyUser legacy(String... data) {
        return new UserData.LegacyUser(data);
    }

    @Test
    void adaptsNamesAndEmailInOrder() {
        assertThat(adapter.adapt(legacy("Ada", "Lovelace", "ada@example.org")))
                .isEqualTo(new UserData.ModernUser("Ada Lovelace", "ada@example.org"));

        List<UserData.ModernUser> all = adapter.adaptAll(List.of(
                legacy("Grace", "Hopper", "grace@example.org"),
                legacy("Ada", "Lovelace", "ada@example.org"),
                legacy("Linus", "Torvalds", "linus@example.org")));

        assertThat(all).containsExactly(
                new UserData.ModernUser("Grace Hopper", "grace@example.org"),
                new UserData.ModernUser("Ada Lovelace", "ada@example.org"),
                new UserData.ModernUser("Linus Torvalds", "linus@example.org"));
    }

    @Test
    void extraFieldsAreIgnored() {
        assertThat(adapter.adapt(legacy("Alan", "Turing", "alan@example.org", "x", "y")))
                .isEqualTo(new UserData.ModernUser("Alan Turing", "alan@example.org"));
    }

    @Test
    void aShortArrayIsRefusedWithIllegalArgument() {
        assertThatIllegalArgumentException().isThrownBy(() -> adapter.adapt(legacy("Grace", "Hopper")));
        assertThatIllegalArgumentException().isThrownBy(() -> adapter.adapt(legacy()));
    }

    @Test
    void anEmptyListAdaptsToAnEmptyList() {
        assertThat(adapter.adaptAll(List.of())).isEmpty();
    }
}
