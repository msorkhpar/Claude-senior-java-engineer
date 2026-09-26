package practice;

public final class ReentrantMutex {

    /** Takes the mutex, waiting while another thread holds it; the holder may take it again. */
    public void lock() throws InterruptedException {
        throw new UnsupportedOperationException("write lock");
    }

    /** Takes the mutex if no other thread holds it. */
    public boolean tryLock() {
        throw new UnsupportedOperationException("write tryLock");
    }

    /** Gives back one hold; the mutex is free at zero. */
    public void unlock() {
        throw new UnsupportedOperationException("write unlock");
    }

    /** The owner's hold count, 0 when free. */
    public int holdCount() {
        throw new UnsupportedOperationException("write holdCount");
    }

    public boolean isHeldByCurrentThread() {
        throw new UnsupportedOperationException("write isHeldByCurrentThread");
    }
}
