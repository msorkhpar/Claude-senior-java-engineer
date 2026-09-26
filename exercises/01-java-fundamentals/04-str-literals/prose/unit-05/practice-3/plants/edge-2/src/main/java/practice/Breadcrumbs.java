package practice;

public final class Breadcrumbs {

    private Breadcrumbs() {
    }

    /** Joins the non-null parts with " / ". */
    public static String trail(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                continue;
            }
            if (i > 0) {
                sb.append(" / ");
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
