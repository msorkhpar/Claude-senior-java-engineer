package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonValueTest {

    @Test
    void writesTypedFieldsAsJson() {
        int quantity = 12;
        long orderNumber = 3_000_000_123L;
        String label = "say \"hi\"";

        assertThat(JsonValue.of(quantity)).isEqualTo("12");
        assertThat(JsonValue.of(orderNumber)).isEqualTo("3000000123");
        assertThat(JsonValue.of(label)).isEqualTo("\"say \\\"hi\\\"\"");
        assertThat(JsonValue.of(0.25)).isEqualTo("0.25");
        assertThat(JsonValue.of(false)).isEqualTo("false");
    }

    @Test
    void writesRowFieldsThatAreNotNumbersAsText() {
        Map<String, Object> row = new HashMap<>();
        row.put("sku", "LAMP-04");
        row.put("note", null);
        row.put("grade", 'B');

        assertThat(JsonValue.of(row.get("sku"))).isEqualTo("\"LAMP-04\"");
        assertThat(JsonValue.of(row.get("note"))).isEqualTo("null");
        assertThat(JsonValue.of(row.get("grade"))).isEqualTo("\"B\"");
    }

    @Test
    void writesASingleCharacterAsAString() {
        char grade = 'B';
        char separator = ';';

        assertThat(JsonValue.of(grade)).isEqualTo("\"B\"");
        assertThat(JsonValue.of(separator)).isEqualTo("\";\"");
    }

    @Test
    void writesNumbersReadFromARowTheSameWayAsTypedOnes() {
        Map<String, Object> row = new HashMap<>();
        row.put("quantity", 12);
        row.put("orderNumber", 3_000_000_123L);
        row.put("discount", 0.25);
        row.put("gift", true);

        assertThat(JsonValue.of(row.get("quantity"))).isEqualTo("12");
        assertThat(JsonValue.of(row.get("orderNumber"))).isEqualTo("3000000123");
        assertThat(JsonValue.of(row.get("discount"))).isEqualTo("0.25");
        assertThat(JsonValue.of(row.get("gift"))).isEqualTo("true");
    }
}
