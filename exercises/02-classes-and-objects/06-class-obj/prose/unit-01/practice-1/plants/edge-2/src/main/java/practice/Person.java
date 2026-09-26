package practice;

public class Person {
    private final String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        setAge(age);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    public String introduce() {
        return "Hello, my name is " + name + " and I am " + age + " years old.";
    }

    /** A person's address: a class nested inside Person. */
    public static class Address {
        private final String street;
        private final String city;

        public Address(String street, String city) {
            this.street = street;
            this.city = city;
        }

        public String getFullAddress() {
            return street + ", " + city;
        }
    }
}
