package practice;

import java.util.concurrent.CopyOnWriteArrayList;

public final class ConcurrentSubject<T> {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    private final CopyOnWriteArrayList<Observer<T>> observers = new CopyOnWriteArrayList<>();

    public void addObserver(Observer<T> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.addIfAbsent(observer);
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
