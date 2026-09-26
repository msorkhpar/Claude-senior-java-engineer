package practice;

import java.io.IOException;
import java.util.List;

/** One unit of work that may fail with an IOException. */
interface Step {
    void run() throws IOException;
}

public final class Audit {

    private Audit() {
    }

    /** Runs step, logging "ok" or "failed: " + message; any failure is rethrown unchanged. */
    public static void runLogged(Step step, List<String> log) throws Exception {
        try {
            step.run();
        } catch (Exception e) {
            log.add("failed: " + e.getMessage());
            throw e;
        }
        log.add("ok");
    }
}
