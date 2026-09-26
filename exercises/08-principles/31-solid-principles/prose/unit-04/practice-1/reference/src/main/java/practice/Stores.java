package practice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Stores {

    private Stores() {
    }

    public interface Readable<K, V> {
        V read(K key);

        boolean exists(K key);
    }

    public interface Writable<K, V> {
        void write(K key, V value);

        void delete(K key);
    }

    public interface Listable<K> {
        List<K> listKeys();

        int size();

        boolean isEmpty();
    }

    public static final class ReadWriteStore
            implements Readable<String, String>, Writable<String, String>, Listable<String> {
        private final Map<String, String> data = new ConcurrentHashMap<>();

        public String read(String key) {
            return data.get(key);
        }

        public boolean exists(String key) {
            return data.containsKey(key);
        }

        public void write(String key, String value) {
            data.put(key, value);
        }

        public void delete(String key) {
            data.remove(key);
        }

        public List<String> listKeys() {
            return data.keySet().stream().sorted().toList();
        }

        public int size() {
            return data.size();
        }

        public boolean isEmpty() {
            return data.isEmpty();
        }
    }

    public static final class ReadOnlyStore implements Readable<String, String>, Listable<String> {
        private final Map<String, String> data;

        public ReadOnlyStore(Map<String, String> data) {
            this.data = data == null ? Map.of() : Map.copyOf(data);
        }

        public String read(String key) {
            return data.get(key);
        }

        public boolean exists(String key) {
            return data.containsKey(key);
        }

        public List<String> listKeys() {
            return data.keySet().stream().sorted().toList();
        }

        public int size() {
            return data.size();
        }

        public boolean isEmpty() {
            return data.isEmpty();
        }
    }
}
