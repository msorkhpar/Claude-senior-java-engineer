package practice;

import java.util.Map;

public class BytesLoader extends ClassLoader {

    private final Map<String, byte[]> classes;

    public BytesLoader(ClassLoader parent, Map<String, byte[]> classes) {
        super(parent);
        this.classes = Map.copyOf(classes);
    }

    // Parent first, but never checks what this loader already defined.
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            ClassLoader parent = getParent();
            return parent != null ? parent.loadClass(name) : Class.forName(name, false, null);
        } catch (ClassNotFoundException e) {
            return findClass(name);
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
