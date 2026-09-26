package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChecksTest {

    public static final class EvenCheck implements Checks.Check {
        @Override
        public boolean ok(Object value) {
            return value instanceof Integer i && i % 2 == 0;
        }
    }

    @Checks.Constraint(validatedBy = EvenCheck.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Even {
    }

    @Checks.NotBlank
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Username {
    }

    static class Profile {
        @Checks.NotBlank
        private final String name;

        @Checks.Positive
        private final int age;

        @Even
        private final int seats;

        @Checks.Label("Nick")
        private final String nick = null;

        Profile(String name, int age, int seats) {
            this.name = name;
            this.age = age;
            this.seats = seats;
        }
    }

    static class Counter {
        @Checks.Positive
        @Even
        private final int count;

        Counter(int count) {
            this.count = count;
        }
    }

    static class Account {
        @Username
        private final String login;

        Account(String login) {
            this.login = login;
        }
    }

    @Test
    void runsTheCheckEachConstraintNames() {
        assertThat(Checks.failures(new Profile("Ada", 36, 4))).isEmpty();
        assertThat(Checks.failures(new Profile(" ", 0, 3)))
                .containsExactly("age: Positive", "name: NotBlank", "seats: Even");
    }

    @Test
    void everyConstraintOnAFieldIsChecked() {
        assertThat(Checks.failures(new Counter(-3))).containsExactly("count: Even", "count: Positive");
        assertThat(Checks.failures(new Counter(4))).isEmpty();
    }

    @Test
    void aComposedAnnotationBringsItsConstraints() {
        assertThat(Checks.failures(new Account(""))).containsExactly("login: NotBlank");
        assertThat(Checks.failures(new Account("ada"))).isEmpty();
    }
}
