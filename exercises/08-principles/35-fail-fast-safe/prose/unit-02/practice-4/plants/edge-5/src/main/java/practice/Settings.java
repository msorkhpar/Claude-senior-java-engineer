package practice;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class Settings {
    private final Map<String, Optional<String>> m = new HashMap<>();
    public void set(String k, String v) { m.put(Objects.requireNonNull(k), Optional.ofNullable(v)); }
    public void unset(String k) { m.remove(k); }
    public boolean isSet(String k) { return m.containsKey(k); }
    public Optional<String> get(String k) { return m.getOrDefault(k, Optional.empty()); }
}
