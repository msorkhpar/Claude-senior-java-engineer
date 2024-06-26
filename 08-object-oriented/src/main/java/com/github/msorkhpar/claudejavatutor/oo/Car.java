package com.github.msorkhpar.claudejavatutor.oo;

public class Car extends Vehicle implements Drivable {
    public Car(String brand) {
        super(brand);
    }

    @Override
    public void start() {
        System.out.println(brand + " car is starting.");
    }

    @Override
    public void accelerate() {
        System.out.println(brand + " car is accelerating.");
    }

    @Override
    public void brake() {
        System.out.println(brand + " car is braking.");
    }
}