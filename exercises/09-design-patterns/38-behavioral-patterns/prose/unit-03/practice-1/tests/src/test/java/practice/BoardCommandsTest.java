package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardCommandsTest {

    private BoardCommands.Board board;
    private BoardCommands.History history;

    @BeforeEach
    void board() {
        board = new BoardCommands.Board();
        board.addCard("todo", "T-14");
        board.addCard("todo", "T-2");
        board.addCard("doing", "T-7");
        board.addCard("done", "T-11");
        board.addCard("done", "T-5");
        board.archive().add("T-1");
        history = new BoardCommands.History();
    }

    @Test
    void archivingMovesTheColumnsCardsToTheArchive() {
        history.run(new BoardCommands.ArchiveColumn(board, "done"));

        assertThat(board.columns().get("done")).isEmpty();
        assertThat(board.archive()).containsExactly("T-1", "T-11", "T-5");
        assertThat(board.columns().get("todo")).containsExactly("T-14", "T-2");
    }

    @Test
    void archivingTwoColumnsArchivesTheCardsOfBoth() {
        history.run(new BoardCommands.ArchiveColumn(board, "done"));
        history.run(new BoardCommands.ArchiveColumn(board, "doing"));

        assertThat(board.columns().get("done")).isEmpty();
        assertThat(board.columns().get("doing")).isEmpty();
        assertThat(board.archive()).containsExactly("T-1", "T-11", "T-5", "T-7");
    }

    @Test
    void undoLeavesTheOtherColumnsAsTheyWere() {
        history.run(new BoardCommands.ArchiveColumn(board, "done"));

        history.undo();

        assertThat(board.columns()).containsKeys("todo", "doing");
        assertThat(board.columns().get("todo")).containsExactly("T-14", "T-2");
        assertThat(board.columns().get("doing")).containsExactly("T-7");
    }

    @Test
    void undoPutsTheArchivedCardsBackInTheirColumn() {
        history.run(new BoardCommands.ArchiveColumn(board, "done"));
        history.run(new BoardCommands.ArchiveColumn(board, "todo"));

        history.undo();
        history.undo();

        assertThat(board.columns().get("done")).containsExactly("T-11", "T-5");
        assertThat(board.columns().get("todo")).containsExactly("T-14", "T-2");
        assertThat(board.archive()).containsExactly("T-1");
    }

    @Test
    void anArchiveCreatedBeforeACardArrivedBringsThatCardBackToo() {
        BoardCommands.ArchiveColumn archiveDone = new BoardCommands.ArchiveColumn(board, "done");
        board.addCard("done", "T-20");
        history.run(archiveDone);

        history.undo();

        assertThat(board.columns().get("done")).containsExactly("T-11", "T-5", "T-20");
        assertThat(board.archive()).containsExactly("T-1");
    }
}
