package practice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
public final class ReadWriteLockedResource<T> {
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final T resource;
    public ReadWriteLockedResource(T resource) { this.resource = Objects.requireNonNull(resource, "resource"); }
    public <R> R read(Function<T, R> readAction) { rwLock.readLock().lock(); try { return readAction.apply(resource); } finally { rwLock.readLock().unlock(); } }
    public void writeVoid(Consumer<T> writeAction) { rwLock.writeLock().lock(); try { writeAction.accept(resource); } finally { rwLock.writeLock().unlock(); } }
    public static final class UserRepository {
        private final ReadWriteLockedResource<Map<String, String>> users = new ReadWriteLockedResource<>(new HashMap<>());
        public void addUser(String id, String name) { users.writeVoid(map -> map.put(id, name)); }
        public String getUser(String id) { return users.read(map -> map.get(id)); }
        public List<String> getAllUsers() { return users.read(map -> new ArrayList<>(map.values())); }
    }
    public static final class ProductRepository {
        private final ReadWriteLockedResource<Map<String, Double>> products = new ReadWriteLockedResource<>(new HashMap<>());
        public void addProduct(String name, double price) { products.writeVoid(map -> map.put(name, price)); }
        public Double getPrice(String name) { return products.read(map -> map.get(name)); }
    }
}
