package practice;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * The health checker's probe. It talks to each service over a plain socket and writes its
 * HTTP/1.1 GET request by hand, so the request head is built here as text.
 */
public class HealthProbe {

    /** Writes the request for {@code path} on {@code host} to the socket's output stream. */
    public static void send(OutputStream socket, String host, String path) throws IOException {
        socket.write(head(host, path, "application/json").getBytes(StandardCharsets.US_ASCII));
        socket.flush();
    }

    /** The request head: the request line, the Host and Accept headers, and the empty line. */
    static String head(String host, String path, String accept) {
        // TODO: Return the four lines of the head, each ended as HTTP/1.1 requires:
        //         GET <path> HTTP/1.1
        //         Host: <host>
        //         Accept: <accept>
        //         (an empty line)
        //       The probe runs on Linux servers and on developers' Windows laptops alike.
        throw new IllegalStateException("TODO: head");
    }
}
