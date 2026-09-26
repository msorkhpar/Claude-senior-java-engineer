package practice;

import java.util.Date;

public class Membership {

    private final String memberId;
    private final Date since;

    public Membership(String memberId, Date since) {
        this.memberId = memberId;
        this.since = new Date(since.getTime());
    }

    public String getMemberId() {
        return memberId;
    }

    public Date getSince() {
        return since;
    }
}
