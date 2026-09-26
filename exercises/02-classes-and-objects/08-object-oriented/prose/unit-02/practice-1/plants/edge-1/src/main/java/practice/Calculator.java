package practice;

public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public double add(double a, double b) {
        return a + b;
    }

    public int add(int a, int b, int c) {
        return a + b + c;
    }

    public String kind(String s) {
        return "String";
    }

    public String kind(Integer i) {
        return "Integer";
    }

    public String kind(Object o) {
        if (o instanceof String) {
            return "String";
        }
        return "Object";
    }
}
