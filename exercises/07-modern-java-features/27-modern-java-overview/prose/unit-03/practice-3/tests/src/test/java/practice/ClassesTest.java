package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class ClassesTest {

    static final class Plain {
    }

    @Test
    void namedClassesAreNamedAndLoadable() {
        assertThat(Classes.kind(String.class)).isEqualTo("named");
        assertThat(Classes.kind(Plain.class)).isEqualTo("named");
        assertThat(Classes.loadable(String.class)).isTrue();
        assertThat(Classes.loadable(ArrayList.class)).isTrue();
        assertThat(Classes.loadable(Plain.class)).isTrue();
    }

    @Test
    void lambdaClassesAreHidden() {
        Runnable lambda = () -> { };
        Function<String, Integer> reference = String::length;
        assertThat(Classes.kind(lambda.getClass())).isEqualTo("hidden");
        assertThat(Classes.kind(reference.getClass())).isEqualTo("hidden");
    }

    @Test
    void anonymousClassesAreNotHidden() {
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
            }
        };
        Supplier<String> other = new Supplier<>() {
            @Override
            public String get() {
                return "x";
            }
        };
        assertThat(Classes.kind(anonymous.getClass())).isEqualTo("anonymous");
        assertThat(Classes.kind(other.getClass())).isEqualTo("anonymous");
    }

    @Test
    void hiddenClassesAreNotLoadable() {
        Runnable lambda = () -> { };
        assertThat(Classes.loadable(lambda.getClass())).isFalse();
        assertThat(Classes.loadable(int.class)).isFalse();
    }
}
