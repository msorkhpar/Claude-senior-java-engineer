package practice;

import java.util.Map;

public class BytesLoader extends ClassLoader {

    private final Map<String, byte[]> classes;

    public BytesLoader(ClassLoader parent, Map<String, byte[]> classes) {
        super(parent);
        this.classes = Map.copyOf(classes);
    }

    // Child first: its own bytes win over the parent's.
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null && classes.containsKey(name)) {
                c = findClass(name);
            }
            return c != null ? c : super.loadClass(name, resolve);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.get(name);
        if (bytes == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }
}
