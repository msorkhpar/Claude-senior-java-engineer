package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusUploadTest {

    private final StatusUpload upload = new StatusUpload();

    @Test
    void readsAFileWrittenWithLineFeeds() {
        assertThat(upload.statuses("PACKED\nSHIPPED\nDELIVERED\n"))
                .containsExactly("PACKED", "SHIPPED", "DELIVERED");
        assertThat(upload.statuses("RECEIVED")).containsExactly("RECEIVED");
    }

    @Test
    void skipsAnEmptyLine() {
        assertThat(upload.statuses("RECEIVED\n\nRETURNED\n\n"))
                .containsExactly("RECEIVED", "RETURNED");
    }

    @Test
    void readsAFileWrittenOnWindows() {
        assertThat(upload.statuses("SHIPPED\r\nRETURNED\r\n\r\nPACKED\r\n"))
                .containsExactly("SHIPPED", "RETURNED", "PACKED");
    }

    @Test
    void readsAFileWhoseLinesEndInCarriageReturnsOnly() {
        assertThat(upload.statuses("DELIVERED\rPACKED\rRECEIVED"))
                .containsExactly("DELIVERED", "PACKED", "RECEIVED");
    }

    @Test
    void skipsALineOfSpacesAndTabs() {
        assertThat(upload.statuses("PACKED\n  \t \nSHIPPED"))
                .containsExactly("PACKED", "SHIPPED");
    }

    @Test
    void skipsALineOfIdeographicSpaces() {
        assertThat(upload.statuses("SHIPPED\n　　\nDELIVERED"))
                .containsExactly("SHIPPED", "DELIVERED");
    }
}
