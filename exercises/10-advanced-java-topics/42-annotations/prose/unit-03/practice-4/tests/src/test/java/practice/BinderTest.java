package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BinderTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Trimmed {
    }

    static class Handlers {
        public String search(@Binder.RequestParam(name = "q") String q,
                             @Binder.RequestParam(name = "page", required = false) int page) {
            return q + page;
        }

        public String tag(@Binder.RequestParam(name = "t", required = false) String tag) {
            return tag;
        }

        public String find(@Trimmed @Binder.RequestParam(name = "id") String id) {
            return id;
        }
    }

    private static Method method(String name) {
        for (Method m : Handlers.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new AssertionError("no method " + name);
    }

    @Test
    void bindsEachParameterByItsName() {
        assertThat(Binder.bind(method("search"), Map.of("q", "java", "page", "2"))).containsExactly("java", 2);
    }

    @Test
    void aMissingOptionalParameterBindsItsDefault() {
        assertThat(Binder.bind(method("search"), Map.of("q", "java"))).containsExactly("java", 0);
        assertThat(Binder.bind(method("tag"), Map.of())).containsExactly((Object) null);
    }

    @Test
    void aMissingRequiredParameterIsRefused() {
        assertThatThrownBy(() -> Binder.bind(method("search"), Map.of("page", "2")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void otherParameterAnnotationsAreSkipped() {
        assertThat(Binder.bind(method("find"), Map.of("id", "42"))).containsExactly("42");
    }
}
