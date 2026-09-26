package practice;

import java.util.ArrayList;
import java.util.List;

public record Team(String name, List<String> members) {

    /** Keep an unmodifiable copy of the members, so nobody can change them later. */
    public Team {
        members = List.copyOf(members);
    }

    /** A new team with the member appended; this team stays as it is. */
    public Team withMember(String member) {
        List<String> more = new ArrayList<>(members);
        more.add(member);
        return new Team(name, more);
    }
}
