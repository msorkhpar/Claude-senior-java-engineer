package practice;

import java.util.ArrayList;
import java.util.List;

public final class LongLine {

    private static final String DELIMITER = "\"\"\"";
    private static final String INDENT = "        ";

    private LongLine() {
    }

    /** Returns text block source whose value is {@code sentence}, wrapped at {@code width} with line continuations. */
    public static String source(String sentence, int width) {
        String[] words = sentence.split(" ");
        List<String> pieces = new ArrayList<>();
        StringBuilder piece = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = i < words.length - 1 ? words[i] + " " : words[i];
            if (piece.length() > 0 && piece.length() + word.length() > width) {
                pieces.add(piece.toString());
                piece.setLength(0);
            }
            piece.append(word);
        }
        pieces.add(piece.toString());
        StringBuilder source = new StringBuilder(DELIMITER).append('\n');
        for (int i = 0; i < pieces.size(); i++) {
            boolean last = i == pieces.size() - 1;
            source.append(INDENT).append(pieces.get(i)).append(last ? DELIMITER : "\\\n");
        }
        return source.toString();
    }
}
