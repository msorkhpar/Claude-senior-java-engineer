package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public enum LazyDefault {
    EMPTY_LIST(ArrayList::new),
    EMPTY_MAP(HashMap::new),
    BUFFER(StringBuilder::new);

    private final Supplier<?> supplier;

    LazyDefault(Supplier<?> supplier) {
        this.supplier = supplier;

    }

    /** A value made by this constant's supplier. */
    @SuppressWarnings("unchecked")
    public <T> T generate() {
        throw new UnsupportedOperationException("write generate");
    }

    /** A value made by this constant's supplier, checked against {@code type}. */
    @SuppressWarnings("unchecked")
    public <T> T generateAs(Class<T> type) {
        throw new UnsupportedOperationException("write generateAs");
    }
}
