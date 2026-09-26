package practice;

import java.util.List;

/** A user; the email may be null. */
record User(String name, String email) {
}

public final class Emails {

    private Emails() {
    }

    /** The users' emails, lower-cased, without duplicates, sorted; null users and null emails are skipped. */
    public static List<String> normalised(List<User> users) {
        throw new UnsupportedOperationException("write normalised");
    }
}
