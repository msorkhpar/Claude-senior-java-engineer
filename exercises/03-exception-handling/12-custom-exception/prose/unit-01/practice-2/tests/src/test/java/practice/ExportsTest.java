package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ExportsTest {

    /**
     * A failure written by a worker on the previous release: ExportFailedException with
     * serialVersionUID 1L and a String field customerId, for customer cus-0417.
     */
    private static final String PREVIOUS_RELEASE = ""
            + "rO0ABXNyACZwcmFjdGljZS5FeHBvcnRzJEV4cG9ydEZhaWxlZEV4Y2VwdGlvbgAAAAAAAAABAgABTAAK"
            + "Y3VzdG9tZXJJZHQAEkxqYXZhL2xhbmcvU3RyaW5nO3hyABNqYXZhLmxhbmcuRXhjZXB0aW9u0P0fPho7"
            + "HMQCAAB4cgATamF2YS5sYW5nLlRocm93YWJsZdXGNSc5d7jLAwAETAAFY2F1c2V0ABVMamF2YS9sYW5n"
            + "L1Rocm93YWJsZTtMAA1kZXRhaWxNZXNzYWdlcQB+AAFbAApzdGFja1RyYWNldAAeW0xqYXZhL2xhbmcv"
            + "U3RhY2tUcmFjZUVsZW1lbnQ7TAAUc3VwcHJlc3NlZEV4Y2VwdGlvbnN0ABBMamF2YS91dGlsL0xpc3Q7"
            + "eHBzcgATamF2YS5pby5JT0V4Y2VwdGlvbmyAc2RlJfCrAgAAeHEAfgACcQB+AAl0ABVidWNrZXQgcXVv"
            + "dGEgZXhjZWVkZWR1cgAeW0xqYXZhLmxhbmcuU3RhY2tUcmFjZUVsZW1lbnQ7AkYqPDz9IjkCAAB4cAAA"
            + "AABzcgAfamF2YS51dGlsLkNvbGxlY3Rpb25zJEVtcHR5TGlzdHq4F7Q8p57eAgAAeHB4dAAjZXhwb3J0"
            + "IGZhaWxlZCBmb3IgY3VzdG9tZXIgY3VzLTA0MTd1cQB+AAsAAAAAcQB+AA54dAAIY3VzLTA0MTc=";

    private static Object toCoordinator(Object failure) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(failure);
        }
        return read(bytes.toByteArray());
    }

    private static Object read(byte[] bytes) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return in.readObject();
        }
    }

    private static Exports.Customer customer(String id) {
        return new Exports.Customer(id, "eu-west", new Object());
    }

    @Test
    void aFailureReachesTheCoordinatorWithItsMessageAndCause() throws Exception {
        Exports.ExportFailedException failure =
                new Exports.ExportFailedException(customer("cus-3390"), new IOException("upload timed out"));

        Object arrived = toCoordinator(failure);

        assertThat(arrived).isInstanceOf(Exports.ExportFailedException.class);
        Exports.ExportFailedException copy = (Exports.ExportFailedException) arrived;
        assertThat(copy.getMessage()).isEqualTo(failure.getMessage());
        assertThat(copy.getCause()).isInstanceOf(IOException.class).hasMessage("upload timed out");
    }

    @Test
    void theWorkerKnowsWhichCustomerFailed() {
        Exports.ExportFailedException failure =
                new Exports.ExportFailedException(customer("cus-7012"), new IOException("disk full"));

        assertThat(failure.getCustomerId()).isEqualTo("cus-7012");
    }

    @Test
    void theCoordinatorKnowsWhichCustomerToRetry() throws Exception {
        Exports.ExportFailedException failure =
                new Exports.ExportFailedException(customer("cus-5521"), new IOException("connection reset"));

        Exports.ExportFailedException copy = (Exports.ExportFailedException) toCoordinator(failure);

        assertThat(copy.getCustomerId()).isEqualTo("cus-5521");
    }

    @Test
    void aFailureFromAWorkerOnThePreviousReleaseCanBeRead() throws Exception {
        Object arrived = read(Base64.getDecoder().decode(PREVIOUS_RELEASE));

        assertThat(arrived).isInstanceOf(Exports.ExportFailedException.class);
        assertThat(((Exception) arrived).getMessage()).isEqualTo("export failed for customer cus-0417");
    }
}
