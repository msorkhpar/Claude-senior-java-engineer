package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicingTest {

    /** A class of the test's own, compiled against the reader's annotations. */
    static class StatementService {
        @Invoicing.Cached(seconds = 900)
        String monthly(String account) {
            return account;
        }

        @Invoicing.Cached(seconds = 20)
        String latest(String account) {
            return account;
        }

        String archive(String account) {
            return account;
        }
    }

    static class Uncached {
        String plain() {
            return "";
        }
    }

    /** The bytes of a compiled class, as the coverage tool and javap read them. */
    private static String classFile(Class<?> type) throws IOException {
        String name = type.getName().substring(type.getPackageName().length() + 1) + ".class";
        try (InputStream in = type.getResourceAsStream(name)) {
            assertThat(in).as("class file of " + type.getName()).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }

    @Test
    void theCacheLayerFindsEveryCachedMethodOfTheService() {
        assertThat(Invoicing.cacheSeconds(Invoicing.InvoiceService.class))
                .isEqualTo(Map.of("history", 45, "totals", 300));
    }

    @Test
    void theCacheLayerReadsAnotherServiceToo() {
        assertThat(Invoicing.cacheSeconds(StatementService.class))
                .isEqualTo(Map.of("latest", 20, "monthly", 900));
        assertThat(Invoicing.cacheSeconds(Uncached.class)).isEmpty();
    }

    @Test
    void theShippedServiceClassCarriesNoReviewNote() throws IOException {
        String bytes = classFile(Invoicing.InvoiceService.class);

        assertThat(bytes).doesNotContain("partner 12");
        assertThat(bytes).doesNotContain("Lpractice/Invoicing$ReviewNote;");
    }

    @Test
    void theCoverageToolFindsTheGeneratedMarkInTheMapperClassFile() throws IOException {
        assertThat(classFile(Invoicing.InvoiceMapper.class)).contains("Lpractice/Invoicing$Generated;");
    }
}
