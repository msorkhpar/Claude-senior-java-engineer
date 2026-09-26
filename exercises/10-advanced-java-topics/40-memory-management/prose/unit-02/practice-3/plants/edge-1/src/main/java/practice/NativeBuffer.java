package practice;

import java.lang.ref.Cleaner;
import java.util.Objects;

public final class NativeBuffer implements AutoCloseable {

    private final byte[] data;
    private final Cleaner.Cleanable cleanable;
    private final Runnable release;
    private boolean open = true;

    public NativeBuffer(Cleaner cleaner, int size, Runnable release) {
        Objects.requireNonNull(release, "release");
        this.data = new byte[size];
        this.release = release;
        // the action is the caller's Runnable: it holds no reference back to this buffer
        this.cleanable = cleaner.register(this, release);
    }

    public void write(int index, byte value) {
        if (!open) {
            throw new IllegalStateException("buffer is closed");
        }
        data[index] = value;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
        release.run();
    }
}
