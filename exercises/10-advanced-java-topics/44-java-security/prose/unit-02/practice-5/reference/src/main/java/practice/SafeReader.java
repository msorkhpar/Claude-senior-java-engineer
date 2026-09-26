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
            in.setObjectInputFilter(info -> {
                Class<?> type = info.serialClass();
                if (type == null) {
                    return ObjectInputFilter.Status.UNDECIDED;
                }
                return allowed.contains(type) ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
            });
            return in.readObject();
        }
    }
}
