package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ReportGenerationExceptionTest {

    @Test
    void keepsTheMessageAndTheCause() {
        IOException io = new IOException("disk full");
        ReportGenerationException e = new ReportGenerationException("Report failed", io);
        assertThat(e.getMessage()).isEqualTo("Report failed");
        assertThat(e.getCause()).isSameAs(io);
    }

    @Test
    void theNoArgumentFormHasNeitherMessageNorCause() {
        ReportGenerationException e = new ReportGenerationException();
        assertThat(e.getMessage()).isNull();
        assertThat(e.getCause()).isNull();
    }

    @Test
    void theCauseOnlyFormTakesTheCauseText() {
        IOException io = new IOException("disk full");
        ReportGenerationException e = new ReportGenerationException(io);
        assertThat(e.getMessage()).isEqualTo("java.io.IOException: disk full");
        assertThat(e.getCause()).isSameAs(io);
    }

    @Test
    void theMessageOnlyFormLeavesTheCauseUnset() {
        ReportGenerationException e = new ReportGenerationException("Report failed");
        IOException io = new IOException("disk full");
        e.initCause(io);
        assertThat(e.getMessage()).isEqualTo("Report failed");
        assertThat(e.getCause()).isSameAs(io);
    }
}
