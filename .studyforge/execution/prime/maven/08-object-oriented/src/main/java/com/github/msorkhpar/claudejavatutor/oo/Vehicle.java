package com.github.msorkhpar.claudejavatutor.oo;

public abstract class Vehicle {
    protected String brand;

    public Vehicle(String brand) {
        this.brand = brand;
    }

    public abstract void start();

    public void stop() {
        System.out.println(brand + " vehicle is stopping.");
    }
}
