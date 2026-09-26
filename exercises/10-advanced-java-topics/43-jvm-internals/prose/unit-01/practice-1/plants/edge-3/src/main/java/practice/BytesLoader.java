package practice;

import java.util.Map;

public class BytesLoader extends ClassLoader {

    private final Map<String, byte[]> classes;

    public BytesLoader(ClassLoader parent, Map<String, byte[]> classes) {
        super(parent);
        this.classes = Map.copyOf(classes);
    }

    // Never checks that the map has the name.
    @Override
    protected Class<?> findClass(String name) {
        byte[] bytes = classes.get(name);
        return defineClass(name, bytes, 0, bytes.length);
    }
}
