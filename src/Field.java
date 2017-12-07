/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.log10;


public class Field implements DrawListener {
    private static final double LAMBDA = 3.0;
    private static final double MU = 3.0;
    private static final double EPSILON = 2400;
    private static final int SIZE_X = 1500;
    private static final int SIZE_Y = 1000;
    private static final int POWER = -14;
    private static final int SENSIBILITY = -110;

    private static final int pixel_shape = 3;
    private final Point[][] nearest = new Point[SIZE_X][SIZE_Y];  // which point is pixel (i, j) nearest?
    private final Draw draw = new Draw();

    private List<Transmission> transmissionList;
    private List<Sensor> sensorList;
    private int length;
    private int heigth;
    private double sim_time;

    public Field(int length, int height) {
        draw.setCanvasSize(1440, 820);
        draw.setXscale(0, SIZE_X);
        draw.setYscale(0, SIZE_Y);
        draw.addListener(this);
        draw.clear(Color.gray);
        draw.show(20);

        sensorList = new ArrayList<>();
        transmissionList = new ArrayList<>();
        sim_time = 0.0;
        this.length = length;
        this.heigth = height;

        int id = 0;
        for (int i = 300; i <= length; i += 300)
            for (int j = 303; j <= height; j += 303) {
                sensorList.add(new Sensor(id++, Forest.rand(i - 300, i), Forest.rand(j - 303, j), POWER, SENSIBILITY, LAMBDA));
            }
    }

    public List<Sensor> transmitterCandidates() {
        List<Sensor> candidates = new ArrayList<>();
        for(Sensor s : sensorList)
            if (s.getState() == 0) // supponiamo che possa trasmettere solo chi è nello stato attesa
                candidates.add(s); //TODO: ricordiamo che ogni transmitter ha almeno un receiver
        if(candidates.isEmpty())
            return null; //TODO: non può mai accadere
        return candidates;
    }


    void runSimulation() throws InterruptedException {
        sim_time=0.0;
        for (int i = 0; i <= 5000; i++) {
            Transmission endOfTransmissionEvent = null;
            Sensor sender = Collections.min(readyToTransmit(transmitterCandidates()));

            if (!transmissionList.isEmpty())
                endOfTransmissionEvent = Collections.min(transmissionList);

            if (endOfTransmissionEvent == null || sender.getNextTransmission() < endOfTransmissionEvent.getRemainingTime()) {
                tryTransmission(sender);
            }else {
                endOfTransmission(endOfTransmissionEvent);
            }

            Thread.sleep(draw.getSpeed()*10);
        }
    }

    private void tryTransmission(Sensor s) throws InterruptedException {
        if (s.CSMA()) {
            Sensor receiver = s.getReceiver();  //TODO: se non ci sono receiver?
            Transmission t = new Transmission(s, receiver, 0.005); //TODO: da mettere MU
            transmissionList.add(t);
            SNIR();
            colorSensorPoint(t.getSender(), Color.yellow, Color.BLUE);
            colorSensorPoint(t.getReceiver(), Color.BLACK, Color.white);
        }else{
            colorSensorPoint(s, Color.RED, Color.BLUE);
            colorSensorPoint(s, Color.BLACK, Color.BLACK);
        }
        draw.show(20);
        stepForward(s.getNextTransmission());
        s.setNextTransmission(Forest.exp(LAMBDA));
    }



    private void endOfTransmission(Transmission t) throws InterruptedException {
        stepForward(t.getRemainingTime());
        t.getSender().setState(0);
        t.getReceiver().setState(0);
        transmissionList.remove(t);

        if (t.isState())
            colorSensorPoint(t.getSender(), Color.GREEN, Color.GREEN);

        else
            colorSensorPoint(t.getSender(), Color.RED, Color.RED);


        draw.show(80);
        colorSensorPoint(t.getSender(), Color.BLACK, Color.BLACK);
        colorSensorPoint(t.getReceiver(), Color.BLACK, Color.BLACK);

        draw.show(80);
    }

    private void SNIR () throws InterruptedException {
        for(Transmission t:transmissionList){
            if (t.isState() && calculateSNIR(t) <= EPSILON) {
                t.setState(false);
                System.out.println(1);
            }
        }
        for (Transmission t: transmissionList)
            if(!t.isState()) {
                colorSensorPoint(t.getSender(), Color.RED, Color.RED);

            }
        draw.show(20);
    }

    private double calculateSNIR(Transmission t){
        double interference=0;
        for(Transmission t2: transmissionList)
            if (t2 != t) {
                interference += powerReceived(t2.getSender(), t.getReceiver());
            }
        System.out.println(powerReceived(t.getSender(), t.getReceiver()) - interference);
        return powerReceived(t.getSender(),t.getReceiver()) - interference;
    }


    private double powerReceived(Sensor sender,Sensor receiver){
        return sender.getPower()-(20*log10(Forest.D0) + 20*log10(0.9*pow(10,9))- 147.55) - 37*log10(eucDistance(sender,receiver)/Forest.D0);
    }

    private double eucDistance(Sensor s1, Sensor s2){
        return sqrt(pow(s1.getX_position() - s2.getX_position(), 2) + pow(s1.getY_position() - s2.getY_position(), 2));
    }
    private List<Sensor> readyToTransmit(List<Sensor> sensorList) {
        List<Sensor> transmitters = new ArrayList<>();
        for (Sensor s : sensorList) {
            if (s.getState() == 0)
                transmitters.add(s);
        }
        return transmitters;
    }

    private void stepForward(double x) {
        for (Sensor s : sensorList)
            if (s.getState() == 0)
                s.setNextTransmission(s.getNextTransmission() - x); //TODO: decrementiamo solo quelli che sono in attesa
        for (Transmission t : transmissionList)
            t.setRemaining_time(t.getRemainingTime() - x);
        sim_time += x;
    }

    private List<Sensor> findNeighbors(Sensor s1) {
        List<Sensor> neighbors = new ArrayList<>();
        for (Sensor s2 : sensorList) {
            if (s2 != s1) {
                if (eucDistance(s1,s2) <= s1.distance())  //formula calcolo massima distanza di trasmissione SPL
                    neighbors.add(s2);
            }
        }
        return neighbors;
    }


    void displaySensor() {
        System.out.println(sensorList.size());
        for (Sensor s : sensorList) {
            System.out.println("(x: " + s.getX_position() + ", y:" + s.getY_position() + ")");
        }
    }

    void setNeighbors() {
        for (Sensor s : sensorList)
            s.setNeighbors(findNeighbors(s));
    }

    void showNeighborsId(int id) {
        for (Sensor s : sensorList.get(id).getNeighbors())
            System.out.println(s.getId() + " (x: " + s.getX_position() + ", y:" + s.getY_position() + ")");
    }

    public int mediumDistribution() {
        int cont = 0;
        Sensor min = null;
        int max = -1;
        for (Sensor s : sensorList) {
            min = (min == null || (s.getNeighbors().size() < min.getNeighbors().size())) ? s : min;
            max = s.getNeighbors().size() > max ? s.getNeighbors().size() : max;
            cont += s.getNeighbors().size();
        }
        System.out.println("min : " + min.getNeighbors().size() + " (x : " + min.getX_position() + ", y: " + min.getY_position() + ") max : " + max);
        return cont / sensorList.size();
    }

    public int numberDisconnected() {
        int cont = 0;
        for (Sensor s : sensorList) {
            cont += s.getNeighbors().isEmpty() ? 1 : 0;
        }
        return cont;
    }

    public List<Sensor> getSensorList() {
        return sensorList;
    }



    /* -----------------------------------------------------------------------------------------------------------------

                            ,.-·^*ª'` ·,           .-,             ,'´¨';'          ,.-·.
                      .·´ ,·'´:¯'`·,  '\‘        ;  ';\          ,'   ';'\'       /    ;'\'
                    ,´  ,'\:::::::::\,.·\'      ';   ;:'\        ,'   ,'::'\     ;    ;:::\
                   /   /:::\;·'´¯'`·;\:::\°    ';  ';::';      ,'   ,'::::;    ';    ;::::;'
                  ;   ;:::;'          '\;:·´    ';  ';::;     ,'   ,'::::;'      ;   ;::::;
                 ';   ;::/      ,·´¯';  °      ';  ';::;    ,'   ,'::::;'      ';  ;'::::;
                 ';   '·;'   ,.·´,    ;'\         \   '·:_,'´.;   ;::::;‘      ;  ';:::';
                 \'·.    `'´,.·:´';   ;::\'        \·,   ,.·´:';  ';:::';       ';  ;::::;'
                  '\::\¯::::::::';   ;::'; ‘       \:\¯\:::::\`*´\::;  '      \*´\:::;‘
                    `·:\:::;:·´';.·´\::;'            `'\::\;:·´'\:::'\'   '       '\::\:;'
                        ¯      \::::\;'‚                        `*´°            `*´‘
                                 '\:·´'                           '
       ----------------------------------------------------------------------------------------------------------------- */

    /*RIMOSSO ANTI-ALIASING Draw.java line 264 */

    public void drawSensor(Sensor s) {
        Point p = new Point(s.getX_position(), s.getY_position());

        // draw the point afterwards
        draw.setPenRadius(0.003);
        draw.setPenColor(Color.BLACK);
        draw.filledSquare(s.getX_position() / 10, s.getY_position() / 10, pixel_shape);
        draw.circle(s.getX_position() / 10, s.getY_position() / 10, (int) s.distance() / 10);
    }



    private void colorSensorPoint(Sensor s, Color shape, Color point) {
        int x = s.getX_position() / 10;
        int y = s.getY_position() / 10;
        draw.setPenColor(shape);
        draw.circle(x, y, (int) s.distance() / 10);
        draw.setPenColor(point);
        draw.filledSquare(x, y, pixel_shape);
        draw.show();
    }

    // must implement these since they're part of the interface
    public void keyTyped(char c) {
    }

    public void keyPressed(int keycode) {
    }

    public void keyReleased(int keycode) {
    }

    public void mouseDragged(double x, double y) {
    }

    public void mouseReleased(double x, double y) {
    }

    public void mousePressed(double x, double y) {
    }

    void show() {
        draw.show();
    }

}