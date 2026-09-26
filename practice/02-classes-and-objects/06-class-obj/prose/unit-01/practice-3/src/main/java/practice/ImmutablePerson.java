package practice;

import java.util.List;

public class ImmutablePerson {
    String name;
    int age;
    List<String> nicknames;

    public ImmutablePerson(String name, int age, List<String> nicknames) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public String getName() {
        throw new UnsupportedOperationException("write getName");
    }

    public int getAge() {
        throw new UnsupportedOperationException("write getAge");
    }

    public List<String> getNicknames() {
        throw new UnsupportedOperationException("write getNicknames");
    }

    public ImmutablePerson withAge(int age) {
        throw new UnsupportedOperationException("write withAge");
    }
}
