package practice;

import java.lang.ref.Cleaner;

public final class NativeBuffer implements AutoCloseable {

    public NativeBuffer(Cleaner cleaner, int size, Runnable release) {
    }

    public void write(int index, byte value) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isOpen() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("TODO");
    }
}
