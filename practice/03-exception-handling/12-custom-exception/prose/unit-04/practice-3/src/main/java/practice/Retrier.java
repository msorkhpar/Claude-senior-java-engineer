package practice;

/** Checked: a failure that may go away if the call is repeated. */
class TransientException extends Exception {
    TransientException(String message) {
        super(message);
    }
}

/** A call that may fail transiently. */
interface Call<T> {
    T call() throws TransientException;
}

public final class Retrier {

    private Retrier() {
    }

    /** The first successful result within attempts tries; see the statement for each failure. */
    public static <T> T withRetry(Call<T> call, int attempts) throws TransientException {
        throw new UnsupportedOperationException("write withRetry");
    }
}
