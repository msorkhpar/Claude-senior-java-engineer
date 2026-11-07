package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSearchTest {

    private final OrderSearch search = new OrderSearch();

    /** The value reaches the statement, as a bound parameter or written into the text. */
    private static boolean uses(OrderSearch.Query q, String value) {
        return q.params().contains(value) || q.sql().contains(value);
    }

    @Test
    void findsACustomersOrdersSortedAsAsked() {
        OrderSearch.Query q = search.search("jane.doe@example.com", null, "total");

        assertThat(q.sql()).startsWith("SELECT id, customer_email, status, total\nFROM orders\nWHERE customer_email");
        assertThat(q.sql()).endsWith("\nORDER BY total\n");
        assertThat(uses(q, "jane.doe@example.com")).isTrue();
    }

    @Test
    void filtersByStatusAndSortsByPlacedAtByDefault() {
        OrderSearch.Query q = search.search("ops@example.org", "REFUNDED", null);

        assertThat(q.sql()).contains("\n  AND status");
        assertThat(q.sql()).endsWith("\nORDER BY placed_at\n");
        assertThat(uses(q, "ops@example.org")).isTrue();
        assertThat(uses(q, "REFUNDED")).isTrue();
    }

    @Test
    void whatTheAgentTypesNeverChangesTheStatementText() {
        OrderSearch.Query plain = search.search("a.smith@example.net", "SHIPPED", "status");
        OrderSearch.Query hostile = search.search("o'neil@example.net", "x') OR ('a'='a", "status");

        assertThat(hostile.sql()).isEqualTo(plain.sql());
        assertThat(hostile.params()).containsExactly("o'neil@example.net", "x') OR ('a'='a");
    }

    @Test
    void aSortColumnThatIsNotOfferedIsRefused() {
        assertThatThrownBy(() -> search.search("a.smith@example.net", null, "total DESC; DELETE FROM orders"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> search.search("a.smith@example.net", null, "(SELECT 1)"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
