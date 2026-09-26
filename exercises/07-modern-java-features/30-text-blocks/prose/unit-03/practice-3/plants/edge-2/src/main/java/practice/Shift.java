package practice;

public final class Shift {

    private Shift() {
    }

    /** Returns {@code text} with up to {@code n} leading whitespace characters removed from each line. */
    public static String left(String text, int n) {
        String[] lines = text.split("\n", -1);
        int count = text.endsWith("\n") ? lines.length - 1 : lines.length;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String line = lines[i];
            int cut = 0;
            while (cut < n && cut < line.length() && line.charAt(cut) == ' ') {
                cut++;
            }
            out.append(line.substring(cut)).append('\n');
        }
        return out.toString();
    }
}
