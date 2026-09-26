package practice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetainedTest {

    /** Service's compiled class file, as ISO-8859-1 text so descriptors can be searched. */
    private static String serviceClassFile() throws IOException {
        try (InputStream in = Retained.Service.class.getResourceAsStream("Retained$Service.class")) {
            assertThat(in).as("Retained$Service.class on the class path").isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }

    private static String descriptor(Class<?> type) {
        return "L" + type.getName().replace('.', '/') + ";";
    }

    @Test
    void listsTheCachedMethods() {
        assertThat(Retained.cached(Retained.Service.class))
                .containsExactly("config -> default (300s)", "getUsers -> users (600s)");
    }

    @Test
    void reviewNotesStayOutOfTheClassFile() throws IOException {
        assertThat(Retained.ReviewNeeded.class.getAnnotation(Retention.class)).isNotNull();
        assertThat(serviceClassFile()).doesNotContain(descriptor(Retained.ReviewNeeded.class));
    }

    @Test
    void generatedCodeIsInTheClassFileButNotReflective() throws IOException {
        Retention declared = Retained.GeneratedCode.class.getAnnotation(Retention.class);

        assertThat(declared).isNotNull();
        assertThat(declared.value()).isEqualTo(RetentionPolicy.CLASS);
        assertThat(serviceClassFile()).contains(descriptor(Retained.GeneratedCode.class));
        assertThat(Retained.Service.class.isAnnotationPresent(Retained.GeneratedCode.class)).isFalse();
    }
}
