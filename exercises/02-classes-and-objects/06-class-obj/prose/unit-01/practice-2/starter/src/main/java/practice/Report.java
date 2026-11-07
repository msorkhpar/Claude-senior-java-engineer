package practice;

import java.io.Serializable;
import java.util.Locale;

/**
 * A generated sales report and its rendered PDF, which can run to several megabytes. A report is
 * not serializable: it lives only while the request that built it runs.
 */
public class Report {

    private final String currency;
    private final byte[] pdf;

    public Report(String currency, byte[] pdf) {
        this.currency = currency;
        this.pdf = pdf;
    }

    public int pdfSize() {
        return pdf.length;
    }

    /**
     * One line of the report as the UI shows it. The UI keeps the rows a user looked at in their
     * HTTP session, for up to an hour, and the servlet container serializes sessions.
     */
    public class Row implements Serializable {
        private final String sku;
        private final long cents;

        Row(String sku, long cents) {
            this.sku = sku;
            this.cents = cents;
        }

        public String sku() {
            return sku;
        }

        public String price() {
            return String.format(Locale.ROOT, "%d.%02d %s", cents / 100, cents % 100, currency);
        }
    }

    /** A row of this report, priced in the report's currency. */
    public Row row(String sku, long cents) {
        // TODO: Rows end up in the session store, which serializes them, and must not keep the
        //       report (and its PDF) in memory for as long as the session lives. Change Row as
        //       needed, and return a row that prices in this report's currency.
        throw new IllegalStateException("TODO: row");
    }
}
