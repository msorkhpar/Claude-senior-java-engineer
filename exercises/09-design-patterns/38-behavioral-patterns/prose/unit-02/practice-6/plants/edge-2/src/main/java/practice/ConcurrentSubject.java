package practice;

import java.util.ArrayList;
import java.util.List;

public final class ConcurrentSubject<T> {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    private final List<Observer<T>> observers = new ArrayList<>();

    public void addObserver(Observer<T> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String event, T data) {
        for (Observer<T> observer : observers) {
            observer.update(event, data);
        }
    }

    public int observerCount() {
        return observers.size();
    }
}
