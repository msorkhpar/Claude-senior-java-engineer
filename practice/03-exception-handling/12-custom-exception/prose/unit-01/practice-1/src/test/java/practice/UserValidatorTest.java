package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class UserValidatorTest {

    private final UserValidator validator = new UserValidator();

    @Test
    void rejectsAnEmptyUsernameNamingTheUser() {
        assertThatCode(() -> validator.validateUsername("u1", new String("alice"))).doesNotThrowAnyException();
        InvalidUserException e = catchThrowableOfType(
                () -> validator.validateUsername("u7", new String("")), InvalidUserException.class);
        assertThat(e).isNotNull();
        assertThat(e.getUserId()).isEqualTo("u7");
    }

    @Test
    void theMessageReachesTheSuperclass() {
        InvalidUserException e = catchThrowableOfType(
                () -> validator.validateUsername("u2", new String("")), InvalidUserException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Username cannot be null or empty");
        assertThat(e.toString()).endsWith(": Username cannot be null or empty");
    }

    @Test
    void aBlankUsernameIsRejected() {
        InvalidUserException e = catchThrowableOfType(
                () -> validator.validateUsername("u3", new String("  \t \n")), InvalidUserException.class);
        assertThat(e).isNotNull();
        assertThat(e.getUserId()).isEqualTo("u3");
    }

    @Test
    void aNullUsernameIsRejectedTheSameWay() {
        InvalidUserException e = catchThrowableOfType(
                () -> validator.validateUsername("u4", null), InvalidUserException.class);
        assertThat(e).isNotNull();
        assertThat(e.getUserId()).isEqualTo("u4");
    }

    @Test
    void theUserIdFieldIsFinal() {
        InvalidUserException e = catchThrowableOfType(
                () -> validator.validateUsername("u5", new String("")), InvalidUserException.class);
        assertThat(e).isNotNull();
        List<Field> fields = Arrays.stream(InvalidUserException.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
        assertThat(fields).isNotEmpty();
        assertThat(fields).allMatch(f -> Modifier.isFinal(f.getModifiers()));
    }

    @Test
    void unicodeWhitespaceIsBlankToo() {
        InvalidUserException e = catchThrowableOfType(
                () -> validator.validateUsername("u6", new String("\u2003\u2003")), InvalidUserException.class);
        assertThat(e).isNotNull();
        assertThat(e.getUserId()).isEqualTo("u6");
    }
}
