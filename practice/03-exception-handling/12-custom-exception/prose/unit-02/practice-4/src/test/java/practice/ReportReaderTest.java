package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ReportReaderTest {

    @Test
    void readsAReport() throws Exception {
        ReportReader reader = new ReportReader(name -> "revenue up");
        assertThat(reader.read("q3")).isEqualTo("revenue up");
    }

    @Test
    void aFailedFetchBecomesAReportException() {
        ReportReader reader = new ReportReader(name -> {
            throw new IOException("Original IO exception");
        });
        ReportException e = catchThrowableOfType(() -> reader.read("q3"), ReportException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Could not read report q3");
    }

    @Test
    void theIoExceptionIsTheCause() {
        IOException io = new IOException("Original IO exception");
        ReportReader reader = new ReportReader(name -> {
            throw io;
        });
        ReportException e = catchThrowableOfType(() -> reader.read("q3"), ReportException.class);
        assertThat(e).isNotNull();
        assertThat(e.getCause()).isSameAs(io);
        StringWriter trace = new StringWriter();
        e.printStackTrace(new PrintWriter(trace));
        assertThat(trace.toString()).contains("Caused by: java.io.IOException: Original IO exception");
    }

    @Test
    void anUncheckedFailureIsNotWrapped() throws Exception {
        assertThat(new ReportReader(name -> "ok").read("q1")).isEqualTo("ok");
        IllegalArgumentException bug = new IllegalArgumentException("bad name");
        ReportReader reader = new ReportReader(name -> {
            throw bug;
        });
        assertThat(catchThrowable(() -> reader.read("q3"))).isSameAs(bug);
    }
}
