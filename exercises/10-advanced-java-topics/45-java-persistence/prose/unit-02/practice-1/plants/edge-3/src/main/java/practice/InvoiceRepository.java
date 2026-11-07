package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads invoices with their lines for the billing overview. Tables:
 * invoice(id, customer) and invoice_line(invoice_id, line_no, sku, qty).
 */
public class InvoiceRepository {

    public record Line(String sku, int qty) {}

    public static final class Invoice {
        public final long id;
        public final String customer;
        public final List<Line> lines = new ArrayList<>();

        public Invoice(long id, String customer) {
            this.id = id;
            this.customer = customer;
        }
    }

    private final Connection connection;

    public InvoiceRepository(Connection connection) {
        this.connection = connection;
    }

    /** The overview's current loader: right answer, one statement per invoice on top of the first. */
    public List<Invoice> loadAllSlowly() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select id, customer from invoice order by id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) invoices.add(new Invoice(rs.getLong(1), rs.getString(2)));
        }
        for (Invoice invoice : invoices) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select sku, qty from invoice_line where invoice_id = ? order by line_no")) {
                ps.setLong(1, invoice.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) invoice.lines.add(new Line(rs.getString(1), rs.getInt(2)));
                }
            }
        }
        return invoices;
    }

    /** The same result as loadAllSlowly(): every invoice in id order, each with its lines in line order. */
    public List<Invoice> loadAll() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select i.id, i.customer, l.sku, l.qty
                from invoice i left join invoice_line l on l.invoice_id = i.id
                order by i.id, l.line_no""");
             ResultSet rs = ps.executeQuery()) {
            Invoice current = null;
            while (rs.next()) {
                long id = rs.getLong(1);
                current = new Invoice(id, rs.getString(2));
                invoices.add(current);
                String sku = rs.getString(3);
                if (sku != null) current.lines.add(new Line(sku, rs.getInt(4)));
            }
        }
        return invoices;
    }
}
