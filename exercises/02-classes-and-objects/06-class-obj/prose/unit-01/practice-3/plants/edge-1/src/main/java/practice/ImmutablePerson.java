package practice;

import java.util.List;

public final class ImmutablePerson {
    private final String name;
    private final int age;
    private final List<String> nicknames;

    public ImmutablePerson(String name, int age, List<String> nicknames) {
        this.name = name;
        this.age = age;
        this.nicknames = java.util.Collections.unmodifiableList(nicknames);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public ImmutablePerson withAge(int age) {
        return new ImmutablePerson(name, age, nicknames);
    }
}
