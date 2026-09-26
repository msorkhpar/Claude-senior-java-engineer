package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouterTest {

    @Test
    void routesEachItemToOneHandler() {
        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();

        Router.route(List.of("Alice", new String(""), "Bob"), valid::add, invalid::add);

        assertThat(valid).containsExactly("Alice", "Bob");
        assertThat(invalid).containsExactly("");
    }

    @Test
    void whitespaceOnlyIsInvalid() {
        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();

        Router.route(List.of("  ", "Bob", "\t"), valid::add, invalid::add);

        assertThat(valid).containsExactly("Bob");
        assertThat(invalid).containsExactly("  ", "\t");
    }

    @Test
    void nullIsRoutedNotThrown() {
        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();

        Router.route(Arrays.asList("Alice", null, "Bob"), valid::add,
                item -> invalid.add(item == null ? "<null>" : "<blank>"));

        assertThat(valid).containsExactly("Alice", "Bob");
        assertThat(invalid).containsExactly("<null>");
    }
}
