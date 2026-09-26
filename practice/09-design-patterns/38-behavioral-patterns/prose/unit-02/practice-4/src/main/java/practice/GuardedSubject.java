package practice;

public final class GuardedSubject {

    @FunctionalInterface
    public interface Observer {
        void update(String event);
    }

    public void addObserver(Observer observer) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean publish(String event) {
        throw new UnsupportedOperationException("TODO");
    }
}
