package practice;

import java.lang.ref.Cleaner;

/**
 * A buffer in memory the garbage collector doesn't manage: the allocator hands out an address,
 * and the memory stays taken until someone gives the address back with free().
 */
public class NativeBuffer implements AutoCloseable {

    /** Hands out and takes back native memory. Freeing an address twice corrupts the allocator. */
    public interface Allocator {
        long allocate(int bytes);

        void free(long address);
    }

    private final Allocator allocator;
    private final long address;
    private final int size;
    private final Cleaner.Cleanable cleanable;

    public NativeBuffer(Cleaner cleaner, Allocator allocator, int size) {
        this.allocator = allocator;
        this.size = size;
        this.address = allocator.allocate(size);
        this.cleanable = register(cleaner);
    }

    public long address() {
        return address;
    }

    public int size() {
        return size;
    }

    /** Registers what frees this buffer's memory if nobody closes it. */
    Cleaner.Cleanable register(Cleaner cleaner) {
        Allocator owner = allocator;
        long taken = address;
        return cleaner.register(this, () -> owner.free(taken));
    }

    /** Gives the memory back. Closing twice is harmless. */
    @Override
    public void close() {
        cleanable.clean();
    }
}
