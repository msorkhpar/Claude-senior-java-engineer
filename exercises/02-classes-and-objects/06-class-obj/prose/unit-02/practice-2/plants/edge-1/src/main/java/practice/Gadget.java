package practice;

import java.util.ArrayList;
import java.util.List;

public class Gadget {
    private final List<String> steps = new ArrayList<>();
    private final String name;
    private final String colour;

    public Gadget() {
        this("unnamed");
        steps.add(0, "setup");
    }

    public Gadget(String name) {
        this(name, "grey");
        steps.add(0, "setup");
    }

    public Gadget(String name, String colour) {
        steps.add("setup");
        this.name = name;
        this.colour = colour;
        steps.add("named:" + name);
    }

    public String getName() {
        return name;
    }

    public String getColour() {
        return colour;
    }

    public List<String> steps() {
        return List.copyOf(steps);
    }
}
