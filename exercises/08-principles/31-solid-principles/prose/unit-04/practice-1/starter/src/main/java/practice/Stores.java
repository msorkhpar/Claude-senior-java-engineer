package practice;

import java.util.List;
import java.util.Map;

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

        public String read(String key) {
            throw new UnsupportedOperationException("write read");
        }

        public boolean exists(String key) {
            throw new UnsupportedOperationException("write exists");
        }

        public void write(String key, String value) {
            throw new UnsupportedOperationException("write write");
        }

        public void delete(String key) {
            throw new UnsupportedOperationException("write delete");
        }

        public List<String> listKeys() {
            throw new UnsupportedOperationException("write listKeys");
        }

        public int size() {
            throw new UnsupportedOperationException("write size");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("write isEmpty");
        }
    }

    /** Before the split: it implements Writable, whose methods it can only refuse. */
    public static final class ReadOnlyStore
            implements Readable<String, String>, Writable<String, String>, Listable<String> {

        public ReadOnlyStore(Map<String, String> data) {
        }

        public void write(String key, String value) {
            throw new UnsupportedOperationException("read-only");
        }

        public void delete(String key) {
            throw new UnsupportedOperationException("read-only");
        }

        public String read(String key) {
            throw new UnsupportedOperationException("write read");
        }

        public boolean exists(String key) {
            throw new UnsupportedOperationException("write exists");
        }

        public List<String> listKeys() {
            throw new UnsupportedOperationException("write listKeys");
        }

        public int size() {
            throw new UnsupportedOperationException("write size");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("write isEmpty");
        }
    }
}
