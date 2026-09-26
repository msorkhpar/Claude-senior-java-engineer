package practice;

import java.util.ArrayList;
import java.util.List;

public final class GuardedSubject {

    @FunctionalInterface
    public interface Observer {
        void update(String event);
    }

    private final List<Observer> observers = new ArrayList<>();
    private boolean notifying;

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public boolean publish(String event) {
        if (notifying) {
            return false;
        }
        notifying = true;
        try {
            for (Observer observer : new ArrayList<>(observers)) {
                observer.update(event);
                notifying = false;
            }
        } finally {
            notifying = false;
        }
        return true;
    }
}
