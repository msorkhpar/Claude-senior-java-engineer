package practice;

public final class ApiResponse {

    private ApiResponse() {
    }

    /** Returns the JSON response body for {@code status}, {@code message} and {@code data}. */
    public static String json(int status, String message, String data) {
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        // the message is spliced into the template, the rest filled by formatted()
        String template = """
                {
                    "status": %d,
                    "message": "MESSAGE",
                    "data": %s
                }""".replace("MESSAGE", escaped);
        return template.formatted(status, data);
    }
}
