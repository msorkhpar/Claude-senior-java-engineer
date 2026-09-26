package practice;

public final class Breadcrumbs {

    private Breadcrumbs() {
    }

    /** Joins the non-null parts with " / ". */
    public static String trail(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(part);
        }
        return sb.toString();
    }
}
