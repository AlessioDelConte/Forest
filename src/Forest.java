/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.log;

/**
 * @author Alessio Del Conte
 * @author Filippo Maganza
 *
 */

public class Forest {

    public static final int D0 = 100;
    public static final boolean GRAPHICS = true;
    public static final String distType = "random";
    public static final int distance = 500;

    public static void main(String[] args) {
        Field field = new Field(15000, 10000);

        for (Sensor s : field.getSensorList())
            field.drawSensor(s);
        field.show();
        System.out.println(field.getSensorList().size());
        field.runSimulation();

        field.mediumDistribution();
        System.out.println(field.numberDisconnected());

    }

    public static int rand(int i, int j) {
        return ThreadLocalRandom.current().nextInt(i, j);
    }

    public static double exp(double lambda) {
        return -log(ThreadLocalRandom.current().nextDouble(0, 1)) / lambda;
    }
}
