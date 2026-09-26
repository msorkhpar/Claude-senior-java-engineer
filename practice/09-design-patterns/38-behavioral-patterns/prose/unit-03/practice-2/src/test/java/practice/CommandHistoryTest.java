package practice;

import org.junit.jupiter.api.Test;

import practice.CommandHistory.Command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandHistoryTest {

    /** A receiver: a running total. */
    static final class Total {
        long value;
    }

    private static Command add(Total total, long amount) {
        return new Command() {
            @Override
            public void execute() {
                total.value += amount;
            }

            @Override
            public void undo() {
                total.value -= amount;
            }

            @Override
            public String description() {
                return "add " + amount;
            }
        };
    }

    @Test
    void undoesAndRedoesInTurn() {
        Total total = new Total();
        CommandHistory history = new CommandHistory(10);

        history.executeCommand(add(total, 1));
        history.executeCommand(add(total, 20));
        assertThat(total.value).isEqualTo(21);

        assertThat(history.undo()).isTrue();
        assertThat(total.value).isEqualTo(1);
        assertThat(history.canRedo()).isTrue();
        assertThat(history.undo()).isTrue();
        assertThat(total.value).isZero();
        assertThat(history.redoSize()).isEqualTo(2);

        assertThat(history.redo()).isTrue();
        assertThat(total.value).isEqualTo(1);
        assertThat(history.redo()).isTrue();
        assertThat(total.value).isEqualTo(21);
        assertThat(history.undoSize()).isEqualTo(2);
        assertThat(history.canUndo()).isTrue();
        assertThatThrownBy(() -> history.executeCommand(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CommandHistory(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aNewCommandClearsTheRedoStack() {
        Total total = new Total();
        CommandHistory history = new CommandHistory(10);
        history.executeCommand(add(total, 1));
        history.executeCommand(add(total, 20));
        history.undo();

        history.executeCommand(add(total, 300));

        assertThat(history.canRedo()).isFalse();
        assertThat(history.redo()).isFalse();
        assertThat(total.value).isEqualTo(301);
        assertThat(history.getUndoHistory()).containsExactly("add 300", "add 1");
    }

    @Test
    void undoWithNothingToUndoReturnsFalse() {
        CommandHistory history = new CommandHistory(10);

        assertThat(history.undo()).isFalse();
        assertThat(history.redo()).isFalse();
        assertThat(history.canUndo()).isFalse();
    }

    @Test
    void historyListsTheNewestFirst() {
        Total total = new Total();
        CommandHistory history = new CommandHistory(10);

        history.executeCommand(add(total, 1));
        history.executeCommand(add(total, 20));
        history.executeCommand(add(total, 300));

        assertThat(history.getUndoHistory()).containsExactly("add 300", "add 20", "add 1");
    }

    @Test
    void aFullHistoryDropsTheOldest() {
        Total total = new Total();
        CommandHistory history = new CommandHistory(2);

        history.executeCommand(add(total, 1));
        history.executeCommand(add(total, 20));
        history.executeCommand(add(total, 300));

        assertThat(history.undoSize()).isEqualTo(2);
        assertThat(history.getUndoHistory()).containsExactlyInAnyOrder("add 300", "add 20");
        history.undo();
        history.undo();
        assertThat(history.undo()).isFalse();
        assertThat(total.value).isEqualTo(1);
    }
}
