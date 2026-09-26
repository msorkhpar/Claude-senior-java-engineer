package practice;
import java.util.concurrent.ConcurrentHashMap;
public final class NullableMap {
    private static final String NULL = "\u0000";
    private final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
    public void put(String key, String value) { map.put(key, value == null ? NULL : value); }
    public String get(String key) { Object v = map.get(key); return NULL.equals(v) ? null : (String) v; }
    public boolean containsKey(String key) { return map.containsKey(key); }
}
