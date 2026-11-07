package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The team board's edits, each one a command the board's history can undo. */
public class BoardCommands {

    /**
     * A kanban board: named columns of cards, in order, and an archive kept sorted by card id.
     * Like the board service it models, it hands out its live map and lists.
     */
    public static class Board {
        private final Map<String, List<String>> columns = new LinkedHashMap<>();
        private final List<String> archive = new ArrayList<>();

        public Map<String, List<String>> columns() { return columns; }

        public List<String> archive() { return archive; }

        public void addCard(String column, String card) {
            columns.computeIfAbsent(column, c -> new ArrayList<>()).add(card);
        }

        /** Moves every card of the column to the archive, which stays sorted. */
        public void archiveColumn(String column) {
            List<String> cards = columns.get(column);
            if (cards == null) return;
            archive.addAll(cards);
            archive.sort(null);
            cards.clear();
        }

        /** Replaces the whole board with the cards of the given columns and archive. */
        public void restore(Map<String, List<String>> savedColumns, List<String> savedArchive) {
            columns.clear();
            savedColumns.forEach((name, cards) -> columns.put(name, new ArrayList<>(cards)));
            archive.clear();
            archive.addAll(savedArchive);
        }
    }

    public interface Command {
        void execute();
        void undo();
    }

    /** Runs commands and undoes them, most recent first. */
    public static class History {
        private final Deque<Command> done = new ArrayDeque<>();

        public void run(Command command) {
            command.execute();
            done.push(command);
        }

        public boolean undo() {
            if (done.isEmpty()) return false;
            done.pop().undo();
            return true;
        }
    }

    /** "Archive column": the button at the top of every column. */
    public static class ArchiveColumn implements Command {
        private final Board board;
        private final String column;
        private Map<String, List<String>> savedColumns;
        private List<String> savedArchive;

        public ArchiveColumn(Board board, String column) {
            this.board = board;
            this.column = column;
        }

        @Override
        public void execute() {
            savedColumns = new LinkedHashMap<>(board.columns());
            savedArchive = new ArrayList<>(board.archive());
            board.archiveColumn(column);
        }

        @Override
        public void undo() {
            board.restore(savedColumns, savedArchive);
        }
    }
}
