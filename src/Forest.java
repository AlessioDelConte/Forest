/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.log;

/**
 *
 * @author Alessio
 */
public class Forest {



    public static void main(String[] args) throws InterruptedException {
        int id = 0;
        Double sim_time=0.0;

        Field field = new Field (15000,10000);
        //field.displaySensor();
        field.setNeighbors();
        field.showNeighborsId(10);
        /*field.getSensorList().indexOf(Collections.min(field.getSensorList()));*/


        double step=Collections.min(field.getSensorList()).getNext_transmission();
        sim_time += step ;
        Thread.sleep(1000);

        field.show();
        System.out.println(field.mediumDistribution());
        System.out.println(field.numberDisconnected());
        
    }
    
    public static int rand(int i, int j){
        return ThreadLocalRandom.current().nextInt(i, j);
    }

    public static double exp(double lambda){
        return -log(ThreadLocalRandom.current().nextDouble(0, 1)) / lambda;
    }
}
