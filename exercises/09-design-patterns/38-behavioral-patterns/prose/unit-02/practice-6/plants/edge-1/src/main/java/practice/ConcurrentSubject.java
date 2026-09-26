package practice;

import java.util.ArrayList;
import java.util.List;

public final class ConcurrentSubject<T> {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    private final List<Observer<T>> observers = new ArrayList<>();

    public synchronized void addObserver(Observer<T> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    public synchronized void notifyObservers(String event, T data) {
        for (Observer<T> observer : observers) {
            observer.update(event, data);
        }
    }

    public synchronized int observerCount() {
        return observers.size();
    }
}
