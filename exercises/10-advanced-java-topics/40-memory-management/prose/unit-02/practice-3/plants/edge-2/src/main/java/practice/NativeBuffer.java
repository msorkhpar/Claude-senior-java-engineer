package practice;

import java.lang.ref.Cleaner;
import java.util.Objects;

public final class NativeBuffer implements AutoCloseable {

    private final byte[] data;
    private final Cleaner.Cleanable cleanable;
    private boolean open = true;

    public NativeBuffer(Cleaner cleaner, int size, Runnable release) {
        Objects.requireNonNull(release, "release");
        this.data = new byte[size];
        // the action is the caller's Runnable: it holds no reference back to this buffer
        this.cleanable = cleaner.register(this, release);
    }

    public void write(int index, byte value) {
        data[index] = value;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
        cleanable.clean();
    }
}
