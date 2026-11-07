package practice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordingFile;

/**
 * Reads a JFR recording and estimates how many bytes each method of the application allocated
 * on one thread, from the recording's jdk.ObjectAllocationSample events.
 */
public class AllocationReport {

    /**
     * Bytes allocated on {@code thread}, keyed by "class.method" of the application method that
     * made the allocation. Application classes are those in {@code appPackage} or below it.
     */
    public static Map<String, Long> bytesByMethod(Path recording, String thread, String appPackage)
            throws IOException {
        Map<String, Long> report = new TreeMap<>();
        for (RecordedEvent e : RecordingFile.readAllEvents(recording)) {
            if (!e.getEventType().getName().equals("jdk.ObjectAllocationSample")) continue;
            RecordedThread t = e.getThread("eventThread");
            if (t == null || !thread.equals(t.getJavaName()) || e.getStackTrace() == null) continue;
            String method = allocatingMethod(e.getStackTrace().getFrames(), appPackage);
            if (method != null) report.merge(method, bytes(e), Long::sum);
        }
        return report;
    }

    /** The application method charged with a sample, as "class.method", or null if there is none. */
    static String allocatingMethod(List<RecordedFrame> frames, String appPackage) {
        // TODO: Each sample carries the stack of the allocation. Name the method of ours that
        //       made it, as "class.method" (f.getMethod().getType().getName() gives the class),
        //       so that the report points at code we can change.
        throw new IllegalStateException("TODO: allocatingMethod");
    }

    /** How many allocated bytes one sample accounts for. */
    static long bytes(RecordedEvent sample) {
        // TODO: The report adds this up per method, and the sum should come close to what the
        //       method really allocated.
        throw new IllegalStateException("TODO: bytes");
    }
}
