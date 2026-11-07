package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UndoHistoryTest {

    private static List<String> undoAll(UndoHistory history) {
        List<String> undone = new ArrayList<>();
        for (int i = 0; i < 20; i++) history.undo().ifPresent(undone::add);
        return undone;
    }

    @Test
    void everyRecordedEditCanBeUndoneOnce() {
        UndoHistory history = new UndoHistory(50);
        history.record("rename field 'qty'");
        history.record("delete row 7");
        history.record("paste 3 rows");

        assertThat(history.size()).isEqualTo(3);
        List<String> undone = new ArrayList<>();
        for (int i = 0; i < 3; i++) undone.add(history.undo().orElseThrow());

        assertThat(undone).containsExactlyInAnyOrder("rename field 'qty'", "delete row 7", "paste 3 rows");
        assertThat(history.size()).isZero();
    }

    @Test
    void undoTakesBackTheLatestEditFirst() {
        UndoHistory history = new UndoHistory(50);
        history.record("set width 120");
        history.record("set colour teal");
        history.record("hide column B");
        history.record("sort by date");

        assertThat(history.menu(2)).containsExactly("sort by date", "hide column B");
        assertThat(history.undo()).contains("sort by date");
        assertThat(history.undo()).contains("hide column B");
        history.record("freeze row 1");
        assertThat(history.undo()).contains("freeze row 1");
        assertThat(history.undo()).contains("set colour teal");
    }

    @Test
    void undoWithNothingLeftIsEmpty() {
        UndoHistory history = new UndoHistory(5);
        assertThat(history.undo()).isEqualTo(Optional.empty());

        history.record("merge cells A1:B2");
        assertThat(history.undo()).contains("merge cells A1:B2");
        assertThat(history.undo()).isEqualTo(Optional.empty());
    }

    @Test
    void aFullHistoryForgetsTheOldestEdit() {
        UndoHistory history = new UndoHistory(3);
        for (String edit : List.of("e1", "e2", "e3", "e4", "e5")) history.record(edit);

        assertThat(history.size()).isEqualTo(3);
        assertThat(undoAll(history)).containsExactlyInAnyOrder("e5", "e4", "e3");
    }
}
