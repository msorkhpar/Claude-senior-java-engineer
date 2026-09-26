package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    /** The expected body, joined at run time. */
    private static String body(int status, String messageLine, String data) {
        return String.join("\n", "{", "    \"status\": " + status + ",", "    \"message\": \"" + messageLine + "\",",
                "    \"data\": " + data, "}");
    }

    @Test
    void fillsTheTemplate() {
        assertThat(ApiResponse.json(200, new String("OK"), new String("[1, 2]"))).isEqualTo(body(200, "OK", "[1, 2]"));
        assertThat(ApiResponse.json(404, new String("Not found"), new String("null")))
                .isEqualTo(body(404, "Not found", "null"));
        assertThat(ApiResponse.json(201, new String("It's created"), new String("{\"id\": 7}")))
                .isEqualTo(body(201, "It's created", "{\"id\": 7}"));
    }

    @Test
    void aQuoteInTheMessageIsEscaped() {
        assertThat(ApiResponse.json(400, new String("She said \"Hi\""), new String("null")))
                .isEqualTo(body(400, "She said \\\"Hi\\\"", "null"));
    }

    @Test
    void aBackslashInTheMessageIsEscapedFirst() {
        assertThat(ApiResponse.json(500, new String("C:\\temp"), new String("null")))
                .isEqualTo(body(500, "C:\\\\temp", "null"));
        assertThat(ApiResponse.json(500, new String("a\\\"b"), new String("null")))
                .isEqualTo(body(500, "a\\\\\\\"b", "null"));
    }

    @Test
    void aPercentInTheMessageStaysText() {
        assertThat(ApiResponse.json(200, new String("50% done"), new String("{}"))).isEqualTo(body(200, "50% done", "{}"));
        assertThat(ApiResponse.json(200, new String("100%"), new String("[]"))).isEqualTo(body(200, "100%", "[]"));
    }
}
