package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs in name order, so the test that tries to change values() runs last. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class TypedKeyTest {

    @Test
    void listsTheKeysInDeclarationOrder() {
        assertThat(TypedKey.values()).containsExactly(TypedKey.PORT, TypedKey.HOST, TypedKey.VERBOSE, TypedKey.ALLOWED_ORIGINS);
        Integer port = TypedKey.PORT.defaultValue();
        List<String> origins = TypedKey.ALLOWED_ORIGINS.defaultValue();
        assertThat(port).isEqualTo(8080);
        assertThat(origins).containsExactly("*");
        assertThat(TypedKey.named("verbose")).contains(TypedKey.VERBOSE);
    }

    @Test
    void namedFindsANameBuiltAtRuntime() {
        String name = String.join(".", "allowed", "origins");
        assertThat(TypedKey.named(name)).contains(TypedKey.ALLOWED_ORIGINS);
    }

    @Test
    void theConstructorIsPrivate() {
        assertThat(TypedKey.values()).hasSize(4);
        for (Constructor<?> constructor : TypedKey.class.getDeclaredConstructors()) {
            assertThat(Modifier.isPrivate(constructor.getModifiers())).as("constructor %s is private", constructor).isTrue();
        }
        for (Method method : TypedKey.class.getDeclaredMethods()) {
            boolean makesKeys = Modifier.isStatic(method.getModifiers()) && method.getReturnType() == TypedKey.class;
            assertThat(makesKeys && Modifier.isPublic(method.getModifiers())).as("public static factory %s", method).isFalse();
        }
    }

    @Test
    void unknownNameIsEmpty() {
        assertThat(TypedKey.named("timeout")).isEmpty();
    }

    @Test
    void valuesCannotChangeTheKeys() {
        List<TypedKey<?>> keys = TypedKey.values();
        assertThatThrownBy(keys::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThat(TypedKey.values()).hasSize(4);
        try {
            TypedKey.values().set(0, TypedKey.HOST);
        } catch (UnsupportedOperationException refused) {
            // a read-only list is one way to keep the keys safe
        }
        assertThat(TypedKey.values()).first().isEqualTo(TypedKey.PORT);
    }
}
