package com.github.msorkhpar.claudejavatutor.encapsulation;

public class AccessModifiersExample {

    public int publicField = 1;
    protected int protectedField = 2;
    int defaultField = 3;
    private int privateField = 4;

    public void publicMethod() {
        System.out.println("This is a public method");
    }

    protected void protectedMethod() {
        System.out.println("This is a protected method");
    }

    void defaultMethod() {
        System.out.println("This is a default method");
    }

    private void privateMethod() {
        System.out.println("This is a private method");
    }

    // This method is used to test access to private method
    public void callPrivateMethod() {
        privateMethod();
    }

    // Inner class to demonstrate access modifiers on nested classes
    public class PublicInnerClass {
    }

    protected class ProtectedInnerClass {
    }

    class DefaultInnerClass {
    }

    private class PrivateInnerClass {
    }
}

// This class is used to test default access within the same package
class DefaultAccessClass {
    void testDefaultAccess(AccessModifiersExample example) {
        example.defaultMethod();
        int value = example.defaultField;
    }
}