package com.github.msorkhpar.claudejavatutor.classobj;

public class StaticInstance {
    // Static variable
    private static int staticCounter = 0;

    // Instance variable
    private int instanceCounter;

    // Static method
    public static void incrementStaticCounter() {
        staticCounter++;
    }

    // Instance method
    public void incrementInstanceCounter() {
        this.instanceCounter++;
    }

    // Static method that returns static variable
    public static int getStaticCounter() {
        return staticCounter;
    }

    // Instance method that returns instance variable
    public int getInstanceCounter() {
        return this.instanceCounter;
    }

    // Static method demonstrating limitation
    public static void staticMethod() {
        // This would cause a compilation error:
        // System.out.println(instanceCounter);
        System.out.println("Static counter: " + staticCounter);
    }

    // Instance method demonstrating access to both static and instance members
    public void instanceMethod() {
        System.out.println("Instance counter: " + this.instanceCounter);
        System.out.println("Static counter: " + staticCounter);
    }

    // Static initializer block
    static {
        System.out.println("Static initializer block called");
        staticCounter = 10;
    }

    // Instance initializer block
    {
        System.out.println("Instance initializer block called");
        instanceCounter = 5;
    }
}