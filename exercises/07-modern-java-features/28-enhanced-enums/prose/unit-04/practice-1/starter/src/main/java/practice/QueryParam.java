package practice;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

/**
 * The query parameters the order-search endpoint accepts. Each one knows the type of its value
 * and how to parse it from the raw query string.
 */
public enum QueryParam {
    PAGE("page", Integer.class, Integer::valueOf),
    LIMIT("limit", Integer.class, Integer::valueOf),
    SINCE("since", LocalDate.class, LocalDate::parse),
    CUSTOMER("customer", String.class, raw -> raw),
    EXPRESS("express", Boolean.class, Boolean::valueOf);

    private final String name;
    private final Class<?> valueType;
    private final Function<String, ?> parser;

    QueryParam(String name, Class<?> valueType, Function<String, ?> parser) {
        this.name = name;
        this.valueType = valueType;
        this.parser = parser;
    }

    /** One line for the endpoint's help page, such as {@code page (Integer)}. */
    public String describe() {
        return name + " (" + valueType.getSimpleName() + ")";
    }

    /**
     * This parameter's value in {@code query}, as the type the caller asks for; {@code null} when
     * the query doesn't have it.
     */
    public <T> T get(Map<String, String> query, Class<T> type) {
        String raw = query.get(name);
        if (raw == null) return null;
        Object value = parser.apply(raw);
        // TODO: Hand the value back as the type the caller asked for. A caller that asks for a
        //       type this parameter's value doesn't have must find out here, in this call, and
        //       not somewhere later in its own code.
        throw new IllegalStateException("TODO: get");
    }

    /** The class whose instances a caller asking for {@code type} can be given. */
    static <T> Class<T> boxed(Class<T> type) {
        // TODO: Callers write `int page = PAGE.get(query, int.class)` as often as they ask for
        //       Integer.class. Answer the class whose instances such a caller can be given.
        throw new IllegalStateException("TODO: boxed");
    }
}
