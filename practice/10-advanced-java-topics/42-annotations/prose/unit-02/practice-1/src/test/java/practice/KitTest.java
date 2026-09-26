package practice;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KitTest {

    private static RetentionPolicy retention(Class<?> type) {
        Retention r = type.getAnnotation(Retention.class);
        return r == null ? RetentionPolicy.CLASS : r.value();
    }

    private static ElementType[] targets(Class<?> type) {
        Target t = type.getAnnotation(Target.class);
        return t == null ? new ElementType[0] : t.value();
    }

    private static Object defaultOf(Class<?> type, String element) throws Exception {
        return type.getMethod(element).getDefaultValue();
    }

    @Test
    void apiEndpointIsADocumentedRuntimeMethodAnnotation() throws Exception {
        Class<?> t = Kit.ApiEndpoint.class;

        assertThat(retention(t)).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(targets(t)).containsExactly(ElementType.METHOD);
        assertThat(t.isAnnotationPresent(Documented.class)).isTrue();
        assertThat(defaultOf(t, "path")).isNull();
        assertThat(defaultOf(t, "method")).isEqualTo(Kit.HttpMethod.GET);
        assertThat(defaultOf(t, "description")).isEqualTo("");
        assertThat(defaultOf(t, "version")).isEqualTo(1);
    }

    @Test
    void producesDefaultsToJson() throws Exception {
        assertThat((String[]) defaultOf(Kit.ApiEndpoint.class, "produces")).containsExactly("application/json");
    }

    @Test
    void authorAppliesToMethodsAndTypes() throws Exception {
        Class<?> t = Kit.Author.class;

        assertThat(retention(t)).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(t.getDeclaredMethods()).extracting(m -> m.getName()).containsExactly("value");
        assertThat(targets(t)).containsExactlyInAnyOrder(ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void threadSafeIsARuntimeMarker() {
        Class<?> t = Kit.ThreadSafe.class;

        assertThat(t.getDeclaredMethods()).isEmpty();
        assertThat(targets(t)).containsExactly(ElementType.TYPE);
        assertThat(retention(t)).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void roleRepeatsThroughRoles() throws Exception {
        Repeatable r = Kit.Role.class.getAnnotation(Repeatable.class);

        assertThat(r).isNotNull();
        assertThat(r.value()).isEqualTo(Kit.Roles.class);
        assertThat(Kit.Roles.class.getMethod("value").getReturnType()).isEqualTo(Kit.Role[].class);
        assertThat(retention(Kit.Role.class)).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(retention(Kit.Roles.class)).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(targets(Kit.Role.class)).containsExactlyInAnyOrder(ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void auditableIsInherited() throws Exception {
        Class<?> t = Kit.Auditable.class;

        assertThat(t.isAnnotationPresent(Inherited.class)).isTrue();
        assertThat(retention(t)).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(targets(t)).containsExactly(ElementType.TYPE);
        assertThat(defaultOf(t, "level")).isEqualTo("INFO");
    }
}
