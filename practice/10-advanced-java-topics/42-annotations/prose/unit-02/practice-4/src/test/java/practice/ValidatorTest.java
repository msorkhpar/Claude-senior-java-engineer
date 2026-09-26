package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorTest {

    static class Signup {
        @Validator.NotEmpty(message = "Name is required")
        private final String name;

        @Validator.Range(min = 0, max = 150, message = "Invalid age")
        private final int age;

        @Validator.NotEmpty(message = "Email is required")
        private final String email;

        Signup(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }

    static class User {
        @Validator.NotEmpty(message = "Name is required")
        private final String name;

        @Validator.Range(min = 0, max = 150, message = "Invalid age")
        private final int age;

        private final String nickname = null;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    private static String lines(Object obj) {
        return Validator.validate(obj).toString();
    }

    @Test
    void reportsAMissingName() {
        assertThat(Validator.validate(new User("Ada", 36))).isEmpty();
        assertThat(lines(new User(null, 36))).isEqualTo("[name: Name is required]");
    }

    @Test
    void aBlankNameIsEmpty() {
        assertThat(lines(new User("   ", 36))).isEqualTo("[name: Name is required]");
        assertThat(lines(new User("\u2003\t", 36))).isEqualTo("[name: Name is required]");
    }

    @Test
    void theRangeIsInclusive() {
        assertThat(Validator.validate(new User("Ada", 0))).isEmpty();
        assertThat(Validator.validate(new User("Ada", 150))).isEmpty();
        assertThat(lines(new User("Ada", -1))).isEqualTo("[age: Invalid age]");
        assertThat(lines(new User("Ada", 151))).isEqualTo("[age: Invalid age]");
    }

    @Test
    void everyViolationIsReported() {
        assertThat(lines(new User("", 200))).isEqualTo("[age: Invalid age, name: Name is required]");
        assertThat(lines(new Signup("", 200, " ")))
                .isEqualTo("[age: Invalid age, email: Email is required, name: Name is required]");
    }
}
