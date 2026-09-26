package practice;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
public class EventBus<E> {
    private final CopyOnWriteArrayList<Consumer<E>> ls = new CopyOnWriteArrayList<>();
    public void subscribe(Consumer<E> l) { ls.add(l); }
    public void unsubscribe(Consumer<E> l) { ls.removeIf(x -> x == l); }
    public void publish(E event) { for (Consumer<E> l : ls) l.accept(event); }
}
