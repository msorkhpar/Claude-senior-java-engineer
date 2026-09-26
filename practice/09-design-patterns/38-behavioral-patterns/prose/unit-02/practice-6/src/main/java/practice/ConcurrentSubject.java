package practice;

public final class ConcurrentSubject<T> {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    public void addObserver(Observer<T> observer) {
        throw new UnsupportedOperationException("TODO");
    }

    public void removeObserver(Observer<T> observer) {
        throw new UnsupportedOperationException("TODO");
    }

    public void notifyObservers(String event, T data) {
        throw new UnsupportedOperationException("TODO");
    }

    public int observerCount() {
        throw new UnsupportedOperationException("TODO");
    }
}
