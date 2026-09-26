package practice;
public final class TableSize {
    private TableSize() {}
    public static int tableSizeFor(int requested) {
        if (requested <= 1) return 1;
        return (int) Math.pow(2, Math.ceil(Math.log(requested) / Math.log(2)));
    }
}
