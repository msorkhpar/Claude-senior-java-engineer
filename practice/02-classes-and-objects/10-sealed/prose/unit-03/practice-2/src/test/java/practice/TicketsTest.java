package practice;

import org.junit.jupiter.api.Test;
import practice.Tickets.Assigned;
import practice.Tickets.Open;
import practice.Tickets.Resolved;
import practice.Tickets.Row;
import practice.Tickets.Ticket;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketsTest {

    @Test
    void loadsEveryConsistentRowIntoItsState() {
        List<Row> rows = List.of(
                new Row("HD-1201", "OPEN", null, null),
                new Row("HD-1202", "ASSIGNED", "Jane Doe", null),
                new Row("HD-1203", "RESOLVED", "Sam Roe", "replaced the router"));

        List<Ticket> tickets = rows.stream().map(Tickets::fromRow).toList();

        assertThat(tickets).containsExactly(
                new Open("HD-1201"),
                new Assigned("HD-1202", "Jane Doe"),
                new Resolved("HD-1203", "Sam Roe", "replaced the router"));
        assertThat(tickets.stream().map(Tickets::toRow).toList()).isEqualTo(rows);
    }

    @Test
    void refusesRowsThatLackTheirStatesData() {
        assertThatThrownBy(() -> Tickets.fromRow(new Row("HD-1310", "ASSIGNED", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tickets.fromRow(new Row("HD-1311", "RESOLVED", "Sam Roe", " ")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tickets.fromRow(new Row("HD-1312", "CLOSED", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void codeCannotBuildATicketWithoutItsData() {
        assertThatThrownBy(() -> new Assigned("HD-1401", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Resolved("HD-1402", "Sam Roe", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesARowCarryingDataItsStateCannotHave() {
        assertThatThrownBy(() -> Tickets.fromRow(new Row("HD-1501", "OPEN", "Jane Doe", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tickets.fromRow(new Row("HD-1502", "ASSIGNED", "Jane Doe", "duplicate")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRefusalNamesTheRowSoItCanBeFixed() {
        assertThatThrownBy(() -> Tickets.fromRow(new Row("HD-1607", "RESOLVED", null, "reset password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HD-1607");
    }
}
