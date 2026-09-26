package practice;

import java.util.Date;

public class Membership {

    private String memberId;
    private final Date since;

    public Membership(String memberId, Date since) {
        this.memberId = memberId;
        this.since = new Date(since.getTime());
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Date getSince() {
        return new Date(since.getTime());
    }
}
