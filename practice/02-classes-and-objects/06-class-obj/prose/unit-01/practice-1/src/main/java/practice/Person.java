package practice;

public class Person {

    public Person(String name, int age) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public String getName() {
        throw new UnsupportedOperationException("write getName");
    }

    public int getAge() {
        throw new UnsupportedOperationException("write getAge");
    }

    public void setAge(int age) {
        throw new UnsupportedOperationException("write setAge");
    }

    public String introduce() {
        throw new UnsupportedOperationException("write introduce");
    }

    /** A person's address: a class nested inside Person. */
    public static class Address {

        public Address(String street, String city) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public String getFullAddress() {
            throw new UnsupportedOperationException("write getFullAddress");
        }
    }
}
