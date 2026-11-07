package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A small in-process event bus. A listener marks each method that receives events with
 * {@code @Subscribe}; the method takes the event as its only parameter, and receives every
 * published event its parameter type accepts.
 */
public class EventBus {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Subscribe {}

    private record Subscription(Object listener, Method method) {}

    private final List<Subscription> subscriptions = new ArrayList<>();

    /** Registers every subscriber method of the listener. */
    public void register(Object listener) {
        for (Method m : subscriberMethods(listener.getClass())) {
            m.setAccessible(true);
            subscriptions.add(new Subscription(listener, m));
        }
    }

    /** Calls every subscriber whose parameter type accepts the event. */
    public void publish(Object event) {
        for (Subscription s : subscriptions) {
            if (!s.method().getParameterTypes()[0].isInstance(event)) continue;
            try {
                s.method().invoke(s.listener(), event);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("subscriber " + s.method() + " failed", e.getCause());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** The methods of a listener class that receive events, each one once. */
    static List<Method> subscriberMethods(Class<?> type) {
        List<Method> found = new ArrayList<>();
        for (Method m : type.getMethods()) {
            if (m.isAnnotationPresent(Subscribe.class) && m.getParameterCount() == 1 && !m.isBridge()) {
                found.add(m);
            }
        }
        return found;
    }
}
