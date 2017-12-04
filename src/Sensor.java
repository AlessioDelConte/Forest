/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.log10;
import static java.lang.Math.log;
import static java.lang.Math.pow;
/**
 *
 * @author Alessio
 */
public class Sensor {
    public int id;
    int x_position;
    int y_position;
    int power;
    int sensibility;
    int state;
    double next_transmission;
    
    List<Sensor> neighbors = new ArrayList<>();
    
    public Sensor(int id, int x_position, int y_position, int power, int sensibility) {
        this.id = id;
        this.x_position = x_position <= Forest.max_x ? x_position : Forest.max_x;
        this.y_position = y_position <= Forest.max_y ? y_position : Forest.max_y;
        this.power = power;
        this.sensibility = sensibility;
        this.next_transmission = -log(ThreadLocalRandom.current().nextDouble(0, 1)) / 7.0;
    }

    public void setNeighbors() {
        neighbors = Forest.field.myNeighbors(this);
    }

    public List<Sensor> getNeighbors() {
        return neighbors;
    }
    
    public double distance(){
        return pow(10, (-sensibility + power - (20*log10(100) + 20*log10(0.9*pow(10,9)) - 147.55))/35)*100;
    }
}
