package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueriesTest {

    @Test
    void buildsBothForms() {
        assertThat(Queries.select("users").lines().map(String::strip))
                .containsExactly("SELECT id, name, email", "FROM users", "WHERE active = true", "ORDER BY name ASC");
        String inline = Queries.inline("orders");
        assertThat(inline).doesNotContain("\n").contains("FROM orders").startsWith("SELECT id, name, ").endsWith("ORDER BY name ASC");
    }

    @Test
    void selectHasNoIncidentalIndentation() {
        String select = Queries.select("users");
        assertThat(select.lines()).allSatisfy(line -> assertThat(line).doesNotStartWith(" "));
        assertThat(select).isEqualTo("SELECT id, name, email\nFROM users\nWHERE active = true\nORDER BY name ASC\n");
    }

    @Test
    void selectEndsWithANewline() {
        assertThat(Queries.select("accounts")).endsWith("ORDER BY name ASC\n");
    }

    @Test
    void inlineIsOneSpacedLine() {
        assertThat(Queries.inline("orders"))
                .isEqualTo("SELECT id, name, email FROM orders WHERE active = true ORDER BY name ASC");
    }
}
