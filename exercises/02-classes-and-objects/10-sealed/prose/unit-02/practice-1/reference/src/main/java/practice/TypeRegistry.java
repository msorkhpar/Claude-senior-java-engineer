package practice;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * The event codec writes a type id with every message and reads it back to pick the class. At
 * startup it registers every class a message can be at run time, so it never meets a class it
 * has no id for.
 */
public class TypeRegistry {

    private final List<Class<?>> registered = new ArrayList<>();

    /** Registers every class a value of {@code root} can have at run time. */
    public static TypeRegistry of(Class<?> root) {
        TypeRegistry registry = new TypeRegistry();
        registry.collect(root);
        return registry;
    }

    /** The registered classes, in no particular order. */
    public List<Class<?>> registered() {
        return List.copyOf(registered);
    }

    /** The id the codec writes for a value: its class's simple name. */
    public String typeId(Object value) {
        if (!registered.contains(value.getClass())) {
            throw new IllegalStateException("no type id for " + value.getClass().getName());
        }
        return value.getClass().getSimpleName();
    }

    /**
     * Registers {@code type} if a value can have exactly this class, then whatever can stand
     * below it. A message whose class the codec has no id for fails in production, so when this
     * hierarchy lets in classes the registry can't know about, refuse at startup with an
     * IllegalArgumentException that names the class where it happens.
     */
    void collect(Class<?> type) {
        int modifiers = type.getModifiers();
        if (!type.isInterface() && !Modifier.isAbstract(modifiers)) {
            registered.add(type);
        }
        if (type.isSealed()) {
            for (Class<?> sub : type.getPermittedSubclasses()) {
                collect(sub);
            }
        } else if (!Modifier.isFinal(modifiers)) {
            throw new IllegalArgumentException(type.getName() + " is open: classes nobody registered can extend it");
        }
    }
}
