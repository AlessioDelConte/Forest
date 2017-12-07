/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.log;

/**
 * @author Alessio
 */

public class Forest {

    public static final int D0 = 100;
    public static final boolean GRAPHICS = false;

    public static void main(String[] args) {
        Field field = new Field(15000, 10000);

        field.setNeighbors();
        if (field.numberDisconnected() > 0)
            System.out.println(field.numberDisconnected());

        for (Sensor s : field.getSensorList())
            field.drawSensor(s);
        field.show();

        field.runSimulation();


        System.out.println(field.mediumDistribution());
        System.out.println(field.numberDisconnected());

    }

    public static int rand(int i, int j) {
        return ThreadLocalRandom.current().nextInt(i, j);
    }

    public static double exp(double lambda) {
        return -log(ThreadLocalRandom.current().nextDouble(0, 1)) / lambda;
    }
}
