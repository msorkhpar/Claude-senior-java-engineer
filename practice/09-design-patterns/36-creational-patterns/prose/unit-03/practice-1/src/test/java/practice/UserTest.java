package practice;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void buildsAUserFromNamedSteps() {
        User alice = User.builder().name("Alice").age(25).email("alice@example.com").active(false).build();
        User bob = User.builder().name("Bob").build();

        assertThat(alice.name()).isEqualTo("Alice");
        assertThat(alice.age()).isEqualTo(25);
        assertThat(alice.email()).isEqualTo(Optional.of("alice@example.com"));
        assertThat(alice.active()).isFalse();
        assertThat(bob.age()).isZero();
        assertThat(bob.email()).isEmpty();
        assertThat(bob.active()).isTrue();
    }

    @Test
    void aBlankNameIsMissing() {
        assertThatThrownBy(() -> User.builder().name("   ").build()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> User.builder().name("").build()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> User.builder().age(3).build()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ageZeroIsFineButNegativeIsNot() {
        assertThat(User.builder().name("Newborn").age(0).build().age()).isZero();
        assertThatThrownBy(() -> User.builder().name("Cy").age(-1).build()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyTheFinalValuesAreValidated() {
        User di = User.builder().name("Di").age(-1).age(30).build();
        User eve = User.builder().name("  ").name("Eve").build();
        User.Builder blank = User.builder().name(" ");

        assertThat(di.age()).isEqualTo(30);
        assertThat(eve.name()).isEqualTo("Eve");
        assertThatThrownBy(blank::build).isInstanceOf(IllegalStateException.class);
    }
}
