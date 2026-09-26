package practice;

/** A supplier that may throw any exception. */
interface CheckedSupplier<T> {
    T get() throws Exception;
}

public final class Unchecked {

    private Unchecked() {
    }

    /** supplier.get(), with any checked failure translated into an unchecked one. */
    public static <T> T get(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Translated exception", e);
        }
    }
}
