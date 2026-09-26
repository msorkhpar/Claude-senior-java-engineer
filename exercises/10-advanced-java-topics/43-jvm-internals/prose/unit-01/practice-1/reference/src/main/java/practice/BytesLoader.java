package practice;

import java.util.Map;

public class BytesLoader extends ClassLoader {

    private final Map<String, byte[]> classes;

    /**
     * @param parent  the parent loader, or {@code null} for the bootstrap loader
     * @param classes binary class name to the bytes of its class file
     */
    public BytesLoader(ClassLoader parent, Map<String, byte[]> classes) {
        super(parent);
        this.classes = Map.copyOf(classes);
    }

    // ClassLoader.loadClass already checks findLoadedClass, then the parent
    // (the bootstrap loader when the parent is null), and only then calls findClass.
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.get(name);
        if (bytes == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }
}
