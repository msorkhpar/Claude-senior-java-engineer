package com.github.msorkhpar.claudejavatutor.oo;

public interface Drivable {
    void accelerate();

    void brake();

    default void honk() {
        System.out.println("Honk honk!");
    }
}