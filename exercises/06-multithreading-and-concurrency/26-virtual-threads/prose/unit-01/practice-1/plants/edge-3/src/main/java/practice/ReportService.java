package practice;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serves report downloads. The web server runs every request on a virtual thread of its own and
 * calls handle() there. Rendering a report is pure computation: tens to hundreds of milliseconds
 * of CPU, no I/O.
 */
public class ReportService implements AutoCloseable {

    /** Turns a report id into PDF bytes. CPU only; safe to call from several threads at once. */
    public interface Renderer {
        byte[] render(String reportId);
    }

    private final Renderer renderer;
    private final ExecutorService renderPool;

    /** cores: how many CPU cores the service may spend on rendering. */
    public ReportService(Renderer renderer, int cores) {
        this.renderer = renderer;
        this.renderPool = newRenderPool(cores);
    }

    /** Called on the request's virtual thread. Returns the rendered report. */
    public byte[] handle(String reportId) throws InterruptedException, ExecutionException {
        if (reportId.isBlank()) throw new IllegalArgumentException("no report id");
        return render(reportId);
    }

    /** Creates the executor, if any, that render() uses. Called once, from the constructor. */
    ExecutorService newRenderPool(int cores) {
        return Executors.newSingleThreadExecutor();
    }

    /** Renders one report for the calling request and returns its bytes. */
    byte[] render(String reportId) throws InterruptedException, ExecutionException {
        return renderPool.submit(() -> renderer.render(reportId)).get();
    }

    @Override
    public void close() {
        if (renderPool != null) renderPool.shutdownNow();
    }
}
