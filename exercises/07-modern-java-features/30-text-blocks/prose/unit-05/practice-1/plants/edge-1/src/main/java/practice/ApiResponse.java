package practice;

public final class ApiResponse {

    private ApiResponse() {
    }

    /** Returns the JSON response body for {@code status}, {@code message} and {@code data}. */
    public static String json(int status, String message, String data) {
        String escaped = message.replace("\\", "\\\\");
        return """
                {
                    "status": %d,
                    "message": "%s",
                    "data": %s
                }""".formatted(status, escaped, data);
    }
}
