package practice;

abstract class PaymentException extends Exception {
    protected PaymentException(String message) {
        super(message);
    }
}

final class CardDeclinedException extends PaymentException {
    CardDeclinedException(String message) {
        super(message);
    }
}

final class PaymentTimeoutException extends PaymentException {
    PaymentTimeoutException(String message) {
        super(message);
    }
}

final class FraudSuspectedException extends PaymentException {
    FraudSuspectedException(String message) {
        super(message);
    }
}

public class PaymentDescriber {

    /** "declined: <message>", "retry later" or "blocked", by the kind of failure. */
    public static String describe(PaymentException e) {
        throw new UnsupportedOperationException("write describe");
    }
}
