/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

/**
 * @author Alessio
 */
public class Sensor implements Comparable<Sensor> {
    private int id;
    private int x_position;
    private int y_position;
    private int power;
    private int sensibility;
    private int state;      // 0: ascolto; 1: trasmissione; 2:ricezione
    private Double nextTransmission;

    private List<Sensor> neighbors = new ArrayList<>();

    Sensor(int id, int x_position, int y_position, int power, int sensibility, double lambda) {
        this.id = id;
        this.x_position = x_position;
        this.y_position = y_position;
        this.power = power;
        this.sensibility = sensibility;
        this.nextTransmission = Forest.exp(lambda);
        this.setState(0);
    }

    public Sensor getReceiver() {
        if (neighbors.isEmpty()) return null;
        else return neighbors.get(Forest.rand(0, neighbors.size()));
    }

    public int getId() {
        return id;
    }

    public int getX_position() {
        return x_position;
    }

    public int getY_position() {
        return y_position;
    }

    public int getPower() {
        return power;
    }

    public int getSensibility() {
        return sensibility;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getNextTransmission() {
        return nextTransmission;
    }

    public void setNextTransmission(Double nextTransmission) {
        this.nextTransmission = nextTransmission;
    }

    public List<Sensor> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Sensor> sensors) {

        neighbors = sensors;
    }

    public double distance() {
        return pow(10, (-sensibility + power - (20 * log10(Forest.D0) + 20 * log10(0.9 * pow(10, 9)) - 147.55)) / 35) * 100;
    }

    @Override
    public int compareTo(Sensor o) {
        return nextTransmission.compareTo(o.getNextTransmission());
    }

    public boolean CSMA() {
        for (Sensor s : neighbors)
            if (s.getState() == 1) return false;
        return true;
    }


}
