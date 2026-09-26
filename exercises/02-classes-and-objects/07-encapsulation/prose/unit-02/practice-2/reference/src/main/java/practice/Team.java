package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team {

    private final String name;
    private final List<String> members;

    public Team(String name, List<String> members) {
        this.name = name;
        this.members = new ArrayList<>(members);
    }

    public String getName() {
        return name;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public List<String> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
