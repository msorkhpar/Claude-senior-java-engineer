package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HandlerTest {

    static final class HealthHandler extends Handler {
        @Override
        protected List<String> paths() {
            return List.of("/health", "/ready");
        }
    }

    static final class InvoiceHandler extends Handler {
        private final String tenant;

        InvoiceHandler(String tenant) {
            this.tenant = tenant;
        }

        @Override
        protected List<String> paths() {
            return List.of("/" + tenant + "/invoices", "/" + tenant + "/invoices/{id}");
        }
    }

    static final class CountingHandler extends Handler {
        int builds;

        @Override
        protected List<String> paths() {
            builds++;
            return List.of("/metrics");
        }
    }

    @Test
    void aHandlerWithFixedPathsServesThem() {
        assertThat(new HealthHandler().routes()).containsExactly("/health", "/ready");
    }

    @Test
    void aTenantHandlerServesItsOwnTenantsPaths() {
        Handler northwind = new InvoiceHandler("northwind");
        Handler contoso = new InvoiceHandler("contoso");

        assertThat(northwind.routes()).containsExactly("/northwind/invoices", "/northwind/invoices/{id}");
        assertThat(contoso.routes()).containsExactly("/contoso/invoices", "/contoso/invoices/{id}");
    }

    @Test
    void reloadsDoNotBuildThePathsAgain() {
        CountingHandler handler = new CountingHandler();

        handler.routes();
        handler.routes();
        List<String> third = handler.routes();

        assertThat(third).containsExactly("/metrics");
        assertThat(handler.builds).isEqualTo(1);
    }
}
