package practice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.Set;

public final class SafeReader {

    private SafeReader() {
    }

    /** Deserializes data, refusing every class that is not in allowed. */
    public static Object read(byte[] data, Set<Class<?>> allowed) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object result = in.readObject();
            if (!allowed.contains(result.getClass())) {
                throw new java.io.InvalidClassException(result.getClass().getName(), "not allowed");
            }
            return result;
        }
    }
}
