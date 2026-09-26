package practice;

public class ThreadSafeCounter {

    private volatile int count = 0;

    public synchronized void incrementCount() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
