package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/** One read-write locking strategy, shared by every repository. */
public final class ReadWriteLockedResource<T> {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final T resource;

    /** Guards this resource; a null resource is refused at once. */
    public ReadWriteLockedResource(T resource) {
        this.resource = resource;
    }

    /** Runs a read under the read lock and returns its result. */
    public <R> R read(Function<T, R> readAction) {
        throw new UnsupportedOperationException("write read");
    }

    /** Runs a write under the write lock. */
    public void writeVoid(Consumer<T> writeAction) {
        throw new UnsupportedOperationException("write writeVoid");
    }

    /** Users by id, locked by the shared wrapper. */
    public static final class UserRepository {
        private final ReadWriteLockedResource<Map<String, String>> users =
                new ReadWriteLockedResource<>(new HashMap<>());

        public void addUser(String id, String name) {
            users.writeVoid(map -> map.put(id, name));
        }

        public String getUser(String id) {
            return users.read(map -> map.get(id));
        }

        public List<String> getAllUsers() {
            return users.read(map -> new ArrayList<>(map.values()));
        }
    }

    /** Prices by product name, locked by the same shared wrapper. */
    public static final class ProductRepository {
        private final ReadWriteLockedResource<Map<String, Double>> products =
                new ReadWriteLockedResource<>(new HashMap<>());

        public void addProduct(String name, double price) {
            products.writeVoid(map -> map.put(name, price));
        }

        public Double getPrice(String name) {
            return products.read(map -> map.get(name));
        }
    }
}
