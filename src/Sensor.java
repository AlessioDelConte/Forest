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
 * @author Filippo Maganza
 * @author Alessio Del Conte
 *
 */

public class Sensor implements Comparable<Sensor> {
    private int id;
    private int x_position;
    private int y_position;
    private double power;
    private int sensibility;
    private int state;      // 0: ascolto; 1: trasmissione; 2:ricezione
    private Double nextTransmission;
    private double distance;

    private List<Sensor> neighbors = new ArrayList<>();

    Sensor(int id, int x_position, int y_position, double power, int sensibility, double lambda, String distType) {
        this.id = id;
        if(distType.equals("random")) {
            this.x_position = Forest.rand(x_position - Forest.distance, x_position);
            this.y_position = Forest.rand(y_position - (Forest.distance + 3), y_position);
        }
        else{
            this.x_position = x_position;
            this.y_position = y_position;
        }
        this.power = power;
        this.sensibility = sensibility;
        this.nextTransmission = Forest.exp(lambda);
        this.setState(0);
        this.distance = pow(10, (-sensibility + power - (20 * log10(Forest.D0) + 20 * log10(0.9 * pow(10, 9)) - 147.55)) / 35) * 100;
    }

    public Sensor getReceiver() {
        return neighbors.get(Forest.rand(0, neighbors.size()));
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

    public double getPower() {
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

    public void setNeighbors(List<Sensor> sensors) { neighbors = sensors; }

    public double getDistance() {
        return distance;
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
