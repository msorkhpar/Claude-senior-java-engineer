package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Every expected value here is what javac 21 made of the same body written as a text block. */
class FixtureLoaderTest {

    @Test
    void aFixtureCheckedOutWithWindowsLineEndingsLoadsAsWritten() {
        String body = "        {\r\n          \"sku\": \"B-12\",\r\n          \"label\": \"Shelf\\tB\"\r\n        }\r\n        ";

        assertThat(FixtureLoader.load(body)).isEqualTo("{\n  \"sku\": \"B-12\",\n  \"label\": \"Shelf\tB\"\n}\n");
    }

    @Test
    void aFixtureClosedOnItsLastLineLoadsAsWritten() {
        String body = "    total: 3 items\n      \"boxed\": yes\n    end";

        assertThat(FixtureLoader.load(body)).isEqualTo("total: 3 items\n  \"boxed\": yes\nend");
    }

    @Test
    void unicodeEscapesBecomeTheirCharacters() {
        String body = "        Gr\\u00fc\\u00dfe aus K\\uu00f6ln\n        Tab:\\u005Ct!\n        ";

        assertThat(FixtureLoader.load(body)).isEqualTo("Gr\u00fc\u00dfe aus K\u00f6ln\nTab:\t!\n");
    }

    @Test
    void paddingAndCarriageReturnsSurvive() {
        String body = "        SKU-7   \\s\\r\n        QTY 3\\r\n        ";

        assertThat(FixtureLoader.load(body)).isEqualTo("SKU-7    \r\nQTY 3\r\n");
    }

    @Test
    void anEscapedBackslashStartsNoUnicodeEscape() {
        String body = "        path: C:\\\\u0041\\\\data\n        ";

        assertThat(FixtureLoader.load(body)).isEqualTo("path: C:\\u0041\\data\n");
    }

    @Test
    void aUnicodeLineBreakCountsWhenTheIndentationIsMeasured() {
        String body = "            Hello\\u000AWorld\n        ";

        assertThat(FixtureLoader.load(body)).isEqualTo("            Hello\nWorld\n");
    }
}
