package practice;

import java.util.ArrayList;
import java.util.List;

public final class SafeSubject<T> {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    private final List<Observer<T>> observers = new ArrayList<>();

    public void addObserver(Observer<T> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.add(observer);
    }

    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    public List<RuntimeException> notifyObservers(String event, T data) {
        List<RuntimeException> failures = new ArrayList<>();
        List<Observer<T>> snapshot = new ArrayList<>(observers);
        for (Observer<T> observer : snapshot) {
            try {
                observer.update(event, data);
            } catch (RuntimeException e) {
                failures.add(e);
            }
        }
        return failures;
    }
}
