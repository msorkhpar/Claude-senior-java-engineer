package practice;

import org.junit.jupiter.api.Test;
import practice.TraceLookup.Traced;

import static org.assertj.core.api.Assertions.assertThat;

class TraceLookupTest {

    @Traced(level = "FULL") interface Ingest { }
    interface BulkIngest extends Ingest { }
    interface Health { }

    @Traced(level = "SAMPLED") static class BaseHandler { }
    static class PlainHandler implements Ingest { }

    @Traced static class PingHandler { }
    @Traced(level = "OFF") static class QuietHandler extends BaseHandler { }
    static class UploadHandler implements Health, Ingest { }
    static class StatusHandler implements Health { }
    static class ShipmentHandler extends BaseHandler { }
    static class InvoiceHandler extends BaseHandler implements Ingest { }
    static class ResizeHandler extends PlainHandler { }
    static class ArchiveHandler implements BulkIngest { }

    private final TraceLookup lookup = new TraceLookup();

    @Test
    void aClassThatDeclaresItIsTracedAtItsLevel() {
        assertThat(lookup.levelOf(PingHandler.class)).contains("SPANS");
        assertThat(lookup.levelOf(QuietHandler.class)).contains("OFF");
    }

    @Test
    void aClassWithNothingAnywhereIsNotTraced() {
        assertThat(lookup.levelOf(StatusHandler.class)).isEmpty();
    }

    @Test
    void anInterfaceTheClassImplementsMarksIt() {
        assertThat(lookup.levelOf(UploadHandler.class)).contains("FULL");
    }

    @Test
    void aSuperclassMarksItsSubclasses() {
        assertThat(lookup.levelOf(ShipmentHandler.class)).contains("SAMPLED");
        assertThat(lookup.levelOf(InvoiceHandler.class)).contains("SAMPLED");
    }

    @Test
    void anInterfaceReachedThroughASuperclassOrAnotherInterfaceMarksIt() {
        assertThat(lookup.levelOf(ResizeHandler.class)).contains("FULL");
        assertThat(lookup.levelOf(ArchiveHandler.class)).contains("FULL");
    }
}
