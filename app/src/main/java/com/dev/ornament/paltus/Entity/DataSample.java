package com.dev.ornament.paltus.Entity;

public class DataSample {
    private double value;
    private double time;

    public DataSample(double time, double value) {
        this.value = value;
        this.time = time;
    }

    @Override
    public String toString() {
        return "DataSample{" + "value=" + value + ", time=" + time + "}\n";
    }

    public double getValue() {
        return value;
    }

    public double getTime() {
        return time;
    }
}
