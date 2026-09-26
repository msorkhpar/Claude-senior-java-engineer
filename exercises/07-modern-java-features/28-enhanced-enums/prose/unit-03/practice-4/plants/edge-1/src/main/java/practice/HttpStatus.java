package practice;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpStatus {
    OK(200, "OK", Category.SUCCESS),
    CREATED(201, "Created", Category.SUCCESS),
    NO_CONTENT(204, "No Content", Category.SUCCESS),
    BAD_REQUEST(400, "Bad Request", Category.CLIENT_ERROR),
    UNAUTHORIZED(401, "Unauthorized", Category.CLIENT_ERROR),
    FORBIDDEN(403, "Forbidden", Category.CLIENT_ERROR),
    NOT_FOUND(404, "Not Found", Category.CLIENT_ERROR),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", Category.SERVER_ERROR),
    BAD_GATEWAY(502, "Bad Gateway", Category.SERVER_ERROR),
    SERVICE_UNAVAILABLE(503, "Service Unavailable", Category.SERVER_ERROR);

    public enum Category { SUCCESS, CLIENT_ERROR, SERVER_ERROR }

    private static final HttpStatus[] VALUES = values();
    private static final Map<Integer, HttpStatus> BY_CODE = Arrays.stream(VALUES)
            .collect(Collectors.toUnmodifiableMap(HttpStatus::code, Function.identity()));

    private final int code;
    private final String reason;
    private final Category category;

    HttpStatus(int code, String reason, Category category) {
        this.code = code;
        this.reason = reason;
        this.category = category;
    }

    public int code() {
        return code;
    }

    public String reason() {
        return reason;
    }

    public Category category() {
        return category;
    }

    /** The status with {@code code}, or empty. */
    public static Optional<HttpStatus> fromCode(int code) {
        return Optional.of(BY_CODE.get(code));
    }

    /** Every status, in declaration order, from the cached array. */
    public static List<HttpStatus> all() {
        return List.of(VALUES);
    }

    /** The statuses of {@code category}, in declaration order. */
    public static List<HttpStatus> byCategory(Category category) {
        return Arrays.stream(VALUES).filter(s -> s.category == category).toList();
    }
}
