package practice;

/**
 * One line of the access audit log: who made the request, with which roles, what they did and
 * whether it was allowed, and on what path. For example {@code k.mensah [billing,support] W+ /invoices/88}.
 */
public final class AuditLine {

    private AuditLine() {
    }

    /**
     * The audit line for one request.
     *
     * @param user    the signed-in user, or {@code null} for an anonymous request
     * @param roles   the user's role names, possibly none
     * @param op      the operation: 'R' read, 'W' write, 'D' delete
     * @param outcome '+' when the request was allowed, '-' when it was refused
     * @param path    the path that was requested
     */
    public static String line(String user, String[] roles, char op, char outcome, String path) {
        return who(user) + " [" + roles(roles) + "] " + code(op, outcome) + " " + path;
    }

    /** The user column. */
    static String who(String user) {
        return user == null ? "-" : user;
    }

    /** The roles column, between the brackets. */
    static String roles(String[] roles) {
        return "" + roles;
    }

    /** The code column: the operation and the outcome, side by side. */
    static String code(char op, char outcome) {
        return "" + op + outcome;
    }
}
