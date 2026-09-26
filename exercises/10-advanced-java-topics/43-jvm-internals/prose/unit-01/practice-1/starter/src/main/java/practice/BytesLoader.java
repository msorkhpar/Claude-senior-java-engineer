package practice;

import java.util.Map;

public class BytesLoader extends ClassLoader {

    /**
     * @param parent  the parent loader, or {@code null} for the bootstrap loader
     * @param classes binary class name to the bytes of its class file
     */
    public BytesLoader(ClassLoader parent, Map<String, byte[]> classes) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new UnsupportedOperationException("TODO");
    }
}
