package practice;

import java.util.ArrayList;
import java.util.List;

public class Gadget {
    private final List<String> steps = new ArrayList<>();
    private final String name;
    private final String colour;

    public Gadget() {
        this("unnamed");
    }

    public Gadget(String name) {
        this(name, "grey");
    }

    public Gadget(String name, String colour) {
        this.name = name;
        this.colour = colour;
        steps.add("named:" + name);
        steps.add("setup");
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
