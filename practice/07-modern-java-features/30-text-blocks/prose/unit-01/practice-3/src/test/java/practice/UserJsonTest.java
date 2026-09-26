package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserJsonTest {

    /** The expected JSON, joined at run time. */
    private static String json(String nameLine, String ageLine) {
        return String.join("\n", "{", nameLine, ageLine, "}");
    }

    @Test
    void buildsTheFourLines() {
        assertThat(UserJson.of(new String("Alice"), 30))
                .isEqualTo(json("    \"name\": \"Alice\",", "    \"age\": 30"));
        assertThat(UserJson.of(new String("Bob"), 25))
                .isEqualTo(json("    \"name\": \"Bob\",", "    \"age\": 25"));
        assertThat(UserJson.of(new String(""), 0))
                .isEqualTo(json("    \"name\": \"\",", "    \"age\": 0"));
        assertThat(UserJson.of(new String("O'Brien"), 40))
                .isEqualTo(json("    \"name\": \"O'Brien\",", "    \"age\": 40"));
    }

    @Test
    void aQuoteInTheNameIsEscaped() {
        assertThat(UserJson.of(new String("She said \"Hi\""), 7))
                .isEqualTo(json("    \"name\": \"She said \\\"Hi\\\"\",", "    \"age\": 7"));
    }

    @Test
    void aBackslashInTheNameIsEscaped() {
        assertThat(UserJson.of(new String("C:\\temp"), 1))
                .isEqualTo(json("    \"name\": \"C:\\\\temp\",", "    \"age\": 1"));
        assertThat(UserJson.of(new String("a\\\"b"), 2))
                .isEqualTo(json("    \"name\": \"a\\\\\\\"b\",", "    \"age\": 2"));
    }
}
