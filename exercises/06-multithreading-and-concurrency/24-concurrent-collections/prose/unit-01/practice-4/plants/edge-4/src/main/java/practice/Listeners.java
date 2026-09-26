package practice;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
public final class Listeners {
    private final CopyOnWriteArrayList<Consumer<String>> listeners = new CopyOnWriteArrayList<>();
    public boolean register(Consumer<String> l) { return listeners.addIfAbsent(l); }
    public boolean unregister(Consumer<String> l) { return listeners.remove(l); }
    public int fire(String event) {
        int called = 0;
        for (Consumer<String> l : listeners) {
            if (!listeners.contains(l)) continue; // skip ones removed meanwhile
            l.accept(event);
            called++;
        }
        return called;
    }
}
