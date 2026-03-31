package com.github.msorkhpar.claudejavatutor.behavioralpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Command Pattern Tests")
class CommandPatternTest {

    // ===================== TextEditor (Receiver) Tests =====================

    @Nested
    @DisplayName("TextEditor")
    class TextEditorTest {

        @Test
        @DisplayName("Should insert text at position")
        void testInsert() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hello");

            assertThat(editor.getContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should insert text at middle position")
        void testInsertMiddle() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hlo");
            editor.insert(1, "el");

            assertThat(editor.getContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should insert at end")
        void testInsertAtEnd() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hello");
            editor.insert(5, " World");

            assertThat(editor.getContent()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should delete text")
        void testDelete() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hello World");
            editor.delete(5, 6);

            assertThat(editor.getContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should throw on negative insert position")
        void testInsertNegativePosition() {
            var editor = new CommandPattern.TextEditor();

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> editor.insert(-1, "text"));
        }

        @Test
        @DisplayName("Should throw on out-of-bounds insert position")
        void testInsertOutOfBounds() {
            var editor = new CommandPattern.TextEditor();

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> editor.insert(1, "text"));
        }

        @Test
        @DisplayName("Should throw on out-of-bounds delete")
        void testDeleteOutOfBounds() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hi");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> editor.delete(0, 5));
        }

        @Test
        @DisplayName("Should track length correctly")
        void testLength() {
            var editor = new CommandPattern.TextEditor();
            assertThat(editor.length()).isEqualTo(0);

            editor.insert(0, "Hello");
            assertThat(editor.length()).isEqualTo(5);
        }
    }

    // ===================== InsertCommand Tests =====================

    @Nested
    @DisplayName("InsertCommand")
    class InsertCommandTest {

        @Test
        @DisplayName("Should execute insert")
        void testExecute() {
            var editor = new CommandPattern.TextEditor();
            var cmd = new CommandPattern.InsertCommand(editor, 0, "Hello");

            cmd.execute();

            assertThat(editor.getContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should undo insert")
        void testUndo() {
            var editor = new CommandPattern.TextEditor();
            var cmd = new CommandPattern.InsertCommand(editor, 0, "Hello");
            cmd.execute();

            cmd.undo();

            assertThat(editor.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return description")
        void testDescription() {
            var editor = new CommandPattern.TextEditor();
            var cmd = new CommandPattern.InsertCommand(editor, 0, "Hello");

            assertThat(cmd.description()).contains("Insert").contains("Hello").contains("0");
        }

        @Test
        @DisplayName("Should throw on null editor")
        void testNullEditor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.InsertCommand(null, 0, "text"));
        }

        @Test
        @DisplayName("Should throw on null text")
        void testNullText() {
            var editor = new CommandPattern.TextEditor();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.InsertCommand(editor, 0, null));
        }
    }

    // ===================== DeleteCommand Tests =====================

    @Nested
    @DisplayName("DeleteCommand")
    class DeleteCommandTest {

        @Test
        @DisplayName("Should execute delete")
        void testExecute() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hello World");
            var cmd = new CommandPattern.DeleteCommand(editor, 5, 6);

            cmd.execute();

            assertThat(editor.getContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should undo delete (restore deleted text)")
        void testUndo() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hello World");
            var cmd = new CommandPattern.DeleteCommand(editor, 5, 6);
            cmd.execute();

            cmd.undo();

            assertThat(editor.getContent()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should throw undo if never executed")
        void testUndoWithoutExecute() {
            var editor = new CommandPattern.TextEditor();
            editor.insert(0, "Hello");
            var cmd = new CommandPattern.DeleteCommand(editor, 0, 3);

            assertThatIllegalStateException()
                    .isThrownBy(cmd::undo);
        }

        @Test
        @DisplayName("Should return description")
        void testDescription() {
            var editor = new CommandPattern.TextEditor();
            var cmd = new CommandPattern.DeleteCommand(editor, 0, 5);

            assertThat(cmd.description()).contains("Delete").contains("5").contains("0");
        }

        @Test
        @DisplayName("Should throw on null editor")
        void testNullEditor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.DeleteCommand(null, 0, 1));
        }
    }

    // ===================== CommandHistory Tests =====================

    @Nested
    @DisplayName("CommandHistory (Invoker)")
    class CommandHistoryTest {

        @Test
        @DisplayName("Should execute and track commands")
        void testExecuteCommand() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();

            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            history.executeCommand(new CommandPattern.InsertCommand(editor, 5, " World"));

            assertThat(editor.getContent()).isEqualTo("Hello World");
            assertThat(history.undoSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should undo last command")
        void testUndo() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();
            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            history.executeCommand(new CommandPattern.InsertCommand(editor, 5, " World"));

            boolean undone = history.undo();

            assertThat(undone).isTrue();
            assertThat(editor.getContent()).isEqualTo("Hello");
            assertThat(history.undoSize()).isEqualTo(1);
            assertThat(history.redoSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should redo undone command")
        void testRedo() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();
            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            history.undo();

            boolean redone = history.redo();

            assertThat(redone).isTrue();
            assertThat(editor.getContent()).isEqualTo("Hello");
            assertThat(history.undoSize()).isEqualTo(1);
            assertThat(history.redoSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("New command should clear redo stack")
        void testNewCommandClearsRedo() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();
            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            history.undo();
            assertThat(history.canRedo()).isTrue();

            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "World"));

            assertThat(history.canRedo()).isFalse();
            assertThat(editor.getContent()).isEqualTo("World");
        }

        @Test
        @DisplayName("Should return false when undo stack is empty")
        void testUndoEmpty() {
            var history = new CommandPattern.CommandHistory();

            assertThat(history.undo()).isFalse();
            assertThat(history.canUndo()).isFalse();
        }

        @Test
        @DisplayName("Should return false when redo stack is empty")
        void testRedoEmpty() {
            var history = new CommandPattern.CommandHistory();

            assertThat(history.redo()).isFalse();
            assertThat(history.canRedo()).isFalse();
        }

        @Test
        @DisplayName("Should support multiple undo operations")
        void testMultipleUndo() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();
            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "A"));
            history.executeCommand(new CommandPattern.InsertCommand(editor, 1, "B"));
            history.executeCommand(new CommandPattern.InsertCommand(editor, 2, "C"));

            history.undo(); // remove C
            history.undo(); // remove B
            history.undo(); // remove A

            assertThat(editor.getContent()).isEmpty();
            assertThat(history.canUndo()).isFalse();
            assertThat(history.redoSize()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return undo history descriptions")
        void testGetUndoHistory() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();
            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            history.executeCommand(new CommandPattern.InsertCommand(editor, 5, " World"));

            List<String> descriptions = history.getUndoHistory();

            assertThat(descriptions).hasSize(2);
            assertThat(descriptions.get(0)).contains("Insert");
        }

        @Test
        @DisplayName("Should throw on null command")
        void testNullCommand() {
            var history = new CommandPattern.CommandHistory();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> history.executeCommand(null));
        }
    }

    // ===================== MacroCommand Tests =====================

    @Nested
    @DisplayName("MacroCommand")
    class MacroCommandTest {

        @Test
        @DisplayName("Should execute all commands in macro")
        void testExecute() {
            var editor = new CommandPattern.TextEditor();
            var macro = new CommandPattern.MacroCommand("test-macro", List.of(
                    new CommandPattern.InsertCommand(editor, 0, "Hello"),
                    new CommandPattern.InsertCommand(editor, 5, " World")
            ));

            macro.execute();

            assertThat(editor.getContent()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should undo all commands in reverse order")
        void testUndo() {
            var editor = new CommandPattern.TextEditor();
            var macro = new CommandPattern.MacroCommand("test-macro", List.of(
                    new CommandPattern.InsertCommand(editor, 0, "Hello"),
                    new CommandPattern.InsertCommand(editor, 5, " World")
            ));
            macro.execute();

            macro.undo();

            assertThat(editor.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return description with macro name and count")
        void testDescription() {
            var editor = new CommandPattern.TextEditor();
            var macro = new CommandPattern.MacroCommand("my-macro", List.of(
                    new CommandPattern.InsertCommand(editor, 0, "A")
            ));

            assertThat(macro.description()).contains("my-macro").contains("1");
        }

        @Test
        @DisplayName("Should track command count")
        void testCommandCount() {
            var editor = new CommandPattern.TextEditor();
            var macro = new CommandPattern.MacroCommand("test", List.of(
                    new CommandPattern.InsertCommand(editor, 0, "A"),
                    new CommandPattern.InsertCommand(editor, 0, "B")
            ));

            assertThat(macro.commandCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw on null name")
        void testNullName() {
            var editor = new CommandPattern.TextEditor();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.MacroCommand(null, List.of(
                            new CommandPattern.InsertCommand(editor, 0, "A")
                    )));
        }

        @Test
        @DisplayName("Should throw on blank name")
        void testBlankName() {
            var editor = new CommandPattern.TextEditor();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.MacroCommand("  ", List.of(
                            new CommandPattern.InsertCommand(editor, 0, "A")
                    )));
        }

        @Test
        @DisplayName("Should throw on null commands list")
        void testNullCommands() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.MacroCommand("macro", null));
        }

        @Test
        @DisplayName("Should throw on empty commands list")
        void testEmptyCommands() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.MacroCommand("macro", Collections.emptyList()));
        }
    }

    // ===================== LambdaCommand Tests =====================

    @Nested
    @DisplayName("LambdaCommand")
    class LambdaCommandTest {

        @Test
        @DisplayName("Should execute lambda action")
        void testExecute() {
            AtomicInteger value = new AtomicInteger(0);
            var cmd = new CommandPattern.LambdaCommand(
                    "increment", () -> value.incrementAndGet(), () -> value.decrementAndGet());

            cmd.execute();

            assertThat(value.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should undo lambda action")
        void testUndo() {
            AtomicInteger value = new AtomicInteger(0);
            var cmd = new CommandPattern.LambdaCommand(
                    "increment", () -> value.incrementAndGet(), () -> value.decrementAndGet());
            cmd.execute();

            cmd.undo();

            assertThat(value.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return description")
        void testDescription() {
            var cmd = new CommandPattern.LambdaCommand("my-action", () -> {}, () -> {});

            assertThat(cmd.description()).isEqualTo("my-action");
        }

        @Test
        @DisplayName("Should use default description when null")
        void testNullDescription() {
            var cmd = new CommandPattern.LambdaCommand(null, () -> {}, () -> {});

            assertThat(cmd.description()).isEqualTo("LambdaCommand");
        }

        @Test
        @DisplayName("Should throw on null execute action")
        void testNullExecute() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.LambdaCommand("test", null, () -> {}));
        }

        @Test
        @DisplayName("Should throw on null undo action")
        void testNullUndo() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommandPattern.LambdaCommand("test", () -> {}, null));
        }
    }

    // ===================== CommandQueue Tests =====================

    @Nested
    @DisplayName("CommandQueue")
    class CommandQueueTest {

        @Test
        @DisplayName("Should execute all queued commands")
        void testExecuteAll() {
            var editor = new CommandPattern.TextEditor();
            var queue = new CommandPattern.CommandQueue();

            queue.enqueue(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            queue.enqueue(new CommandPattern.InsertCommand(editor, 5, " World"));

            int count = queue.executeAll();

            assertThat(count).isEqualTo(2);
            assertThat(editor.getContent()).isEqualTo("Hello World");
            assertThat(queue.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should execute next command one at a time")
        void testExecuteNext() {
            var editor = new CommandPattern.TextEditor();
            var queue = new CommandPattern.CommandQueue();
            queue.enqueue(new CommandPattern.InsertCommand(editor, 0, "A"));
            queue.enqueue(new CommandPattern.InsertCommand(editor, 1, "B"));

            assertThat(queue.executeNext()).isTrue();
            assertThat(editor.getContent()).isEqualTo("A");
            assertThat(queue.size()).isEqualTo(1);

            assertThat(queue.executeNext()).isTrue();
            assertThat(editor.getContent()).isEqualTo("AB");
            assertThat(queue.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return false when executing from empty queue")
        void testExecuteNextEmpty() {
            var queue = new CommandPattern.CommandQueue();

            assertThat(queue.executeNext()).isFalse();
        }

        @Test
        @DisplayName("Should return 0 when executing all on empty queue")
        void testExecuteAllEmpty() {
            var queue = new CommandPattern.CommandQueue();

            assertThat(queue.executeAll()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should track size correctly")
        void testSize() {
            var queue = new CommandPattern.CommandQueue();
            assertThat(queue.size()).isEqualTo(0);
            assertThat(queue.isEmpty()).isTrue();

            var editor = new CommandPattern.TextEditor();
            queue.enqueue(new CommandPattern.InsertCommand(editor, 0, "A"));
            assertThat(queue.size()).isEqualTo(1);
            assertThat(queue.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("Should throw on null command")
        void testNullCommand() {
            var queue = new CommandPattern.CommandQueue();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> queue.enqueue(null));
        }
    }

    // ===================== Integration Test: Full Workflow =====================

    @Nested
    @DisplayName("Integration: Full Editor Workflow")
    class IntegrationTest {

        @Test
        @DisplayName("Should support full edit-undo-redo workflow")
        void testFullWorkflow() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();

            // Type "Hello World"
            history.executeCommand(new CommandPattern.InsertCommand(editor, 0, "Hello"));
            history.executeCommand(new CommandPattern.InsertCommand(editor, 5, " World"));
            assertThat(editor.getContent()).isEqualTo("Hello World");

            // Delete " World"
            history.executeCommand(new CommandPattern.DeleteCommand(editor, 5, 6));
            assertThat(editor.getContent()).isEqualTo("Hello");

            // Undo delete
            history.undo();
            assertThat(editor.getContent()).isEqualTo("Hello World");

            // Redo delete
            history.redo();
            assertThat(editor.getContent()).isEqualTo("Hello");

            // Undo everything
            history.undo(); // undo delete
            history.undo(); // undo " World"
            history.undo(); // undo "Hello"
            assertThat(editor.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should support macro in history with undo/redo")
        void testMacroInHistory() {
            var editor = new CommandPattern.TextEditor();
            var history = new CommandPattern.CommandHistory();

            var macro = new CommandPattern.MacroCommand("hello-world", List.of(
                    new CommandPattern.InsertCommand(editor, 0, "Hello"),
                    new CommandPattern.InsertCommand(editor, 5, " World")
            ));

            history.executeCommand(macro);
            assertThat(editor.getContent()).isEqualTo("Hello World");

            history.undo();
            assertThat(editor.getContent()).isEmpty();

            history.redo();
            assertThat(editor.getContent()).isEqualTo("Hello World");
        }
    }
}
