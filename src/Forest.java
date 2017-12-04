/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.ThreadLocalRandom;
/**
 *
 * @author Alessio
 */
public class Forest {

    public static Field field = new Field();
    public static int max_x = 15000;
    public static int max_y = 10000;
    private static final int POWER = -14;
    private static final int SENSIBILITY = -110;

    public static void main(String[] args) {
        int id = 0;

        for(int i = 1; i <= max_x; i+=300)
            for (int j = 1; j <= max_y; j+=300){
                field.addSensor(new Sensor(id++, rand(0, 300) + i, rand(0, 300) + j, POWER, SENSIBILITY));
            }
        
        //field.displaySensor();
        field.setNeighbors();
        field.showNeighborsId(10);
        
        for (Sensor s : field.getSensorList()){
            field.drawSensor(s);
        }

        field.show();
        System.out.println(field.mediumDistribution());
        System.out.println(field.numberDisconnected());
        for(Sensor s : field.getSensorList()){
            System.out.println(s.next_transmission);
        }
    /*
        while(true){
            int cont = rand(0,3);
            if(cont == 0)
                field.inTransmission(field.getSensorList().get(rand(0, 1700)));
            if(cont == 1)
                field.inReception(field.getSensorList().get(rand(0, 1700)));
            if(cont == 2)
                field.endOfTransmission(field.getSensorList().get(rand(0, 1700)));
        }*/
        
    }
    
    public static int rand(int i, int j){
        return ThreadLocalRandom.current().nextInt(i, j);
    }
}
