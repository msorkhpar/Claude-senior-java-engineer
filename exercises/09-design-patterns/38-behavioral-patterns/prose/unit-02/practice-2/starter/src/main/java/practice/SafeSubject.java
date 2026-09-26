package practice;

import java.util.List;

public final class SafeSubject<T> {

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

    public List<RuntimeException> notifyObservers(String event, T data) {
        throw new UnsupportedOperationException("TODO");
    }
}
