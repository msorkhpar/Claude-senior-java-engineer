package practice;

public class Employee {
    private final String name;
    private final int id;
    private final String department;

    public Employee(String name, int id, String department) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
        this.id = id;
        this.department = department;
    }

    public Employee(String name, int id) {
        this(name, id, "General");
    }

    public Employee(String name) {
        this(name, 0, null);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDepartment() {
        return department;
    }
}
