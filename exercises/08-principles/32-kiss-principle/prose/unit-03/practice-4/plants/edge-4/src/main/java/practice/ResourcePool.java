package practice;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
public class ResourcePool {
    private final int capacity;
    private final Semaphore semaphore;
    public ResourcePool(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
        this.semaphore = new Semaphore(capacity);
    }
    public boolean tryAcquire() {
        try { return semaphore.tryAcquire(300, TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
    }
    public synchronized void release() {
        if (semaphore.availablePermits() >= capacity) throw new IllegalStateException("no matching acquire");
        semaphore.release();
    }
    public int available() { return semaphore.availablePermits(); }
}
