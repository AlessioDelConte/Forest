/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.*;

/**
 * @author Filippo Maganza
 * @author Alessio Del Conte
 */
public class Field implements DrawListener {
    private static final double LAMBDA = 3.0;
    private static final double MU = 150.0;
    private static final double EPSILON = 10;
    private static final int SIZE_X = 1500;
    private static final int SIZE_Y = 1000;
    private static final int SENSIBILITY = -110;
    private static final double POWER = -(-SENSIBILITY - (20 * log10(Forest.D0) + 20 * log10(0.9 * pow(10, 9)) - 147.55) - 35 * log10((Forest.distance * (Forest.distType.equals("random") ? 2 : 1)) / Forest.D0)) + 5; //il minimo per essere sempre collegato è 9.65 con dist casuale blocchi da 300, -20 circa per distribuzione ottima punto preciso

    private static final int pixel_shape = 3;
    private final Draw draw = Forest.GRAPHICS ? new Draw() : null;

    private List<Transmission> transmissionList;
    private List<Sensor> sensorList;

    //statistics
    private double sim_time;
    private double goodTransmissionTime = 0;
    private double numberOfTransmission = 0;
    private double numberOfGoodTransmission = 0;
    private double numberOfCsma = 0;
    private double badTransmissionTime = 0;

    Field(int length, int height) {
        if (Forest.GRAPHICS) {
            draw.setCanvasSize(1440, 830);
            draw.setXscale(0, SIZE_X);
            draw.setYscale(0, SIZE_Y);
            draw.addListener(this);
            draw.clear(Color.gray);
            show(20);
        }

        sensorList = new ArrayList<>();
        transmissionList = new ArrayList<>();
        sim_time = 0.0;
        int id = 0;
        for (int i = Forest.distance; i <= length; i += Forest.distance)
            for (int j = Forest.distance + 3; j <= height; j += Forest.distance + 3)
                sensorList.add(new Sensor(id++, i, j, POWER, SENSIBILITY, LAMBDA, Forest.distType));
        setNeighbors(); //MOLTO PESANTE CON MOLTI SENSORI
    }

    void runSimulation() {
        sim_time = 0.0;
        for (int i = 0; i <= 50000; i++) {
            Transmission endOfTransmissionEvent = null;
            Sensor sender = Collections.min(transmitterCandidates());

            if (!transmissionList.isEmpty())
                endOfTransmissionEvent = Collections.min(transmissionList);

            if (endOfTransmissionEvent == null || sender.getNextTransmission() < endOfTransmissionEvent.getRemainingTime())
                tryTransmission(sender);
            else
                endOfTransmission(endOfTransmissionEvent);

            if (Forest.GRAPHICS) show(draw.getSpeed() * 10);
        }
        System.out.println(" Rapporto buone/totali : " + numberOfGoodTransmission / (numberOfTransmission -
                transmissionList.size()) + "\n CSMA: " + numberOfCsma + "\n sim_time: " + sim_time + "\n trasmissioni ok : " +
                "" + goodTransmissionTime + " trasmissioni bad: " + badTransmissionTime + "\n %: " + goodTransmissionTime / (goodTransmissionTime + badTransmissionTime));
    }

    private void tryTransmission(Sensor s) {
        stepForward(s.getNextTransmission());  // TODO : ???
        if (s.CSMA()) {
            Sensor receiver = s.getReceiver();
            Transmission t = new Transmission(s, receiver, Forest.exp(MU)); //TODO: da mettere MU
            transmissionList.add(t);
            numberOfTransmission++;
            SNIR();

            colorSensor(t.getSender(), Color.yellow, Color.BLUE);
            colorSensor(t.getReceiver(), Color.BLACK, Color.white);

        } else {
            numberOfCsma++;
            colorSensor(s, Color.RED, Color.BLUE);
            colorSensor(s, Color.BLACK, Color.BLACK);
        }
        show(20);

        s.setNextTransmission(Forest.exp(LAMBDA));
    }

    private void endOfTransmission(Transmission t) {
        stepForward(t.getRemainingTime());
        t.getSender().setState(0);
        t.getReceiver().setState(0);
        transmissionList.remove(t);

        if (t.isState()) {
            goodTransmissionTime += t.getTotalTime();
            numberOfGoodTransmission++;
        } else
            badTransmissionTime += t.getTotalTime();

        colorSensor(t.getSender(), Color.BLACK, Color.BLACK);
        colorSensor(t.getReceiver(), Color.BLACK, Color.BLACK);
        show(20);
    }

    private void SNIR() {
        for (Transmission t : transmissionList)
            if (t.isState() && calculateSNIR(t) <= EPSILON)
                t.setState(false);

        if (Forest.GRAPHICS)
            for (Transmission t : transmissionList)
                if (!t.isState())
                    colorSensor(t.getSender(), Color.RED, Color.RED);

        show(20);
    }

    private double calculateSNIR(Transmission newTrans) {
        double interference = 0;
        for (Transmission t2 : transmissionList)
            if (t2 != newTrans)
                interference += 10 * pow(10, powerReceived(t2.getSender(), newTrans.getReceiver()));

        return log10(10 * pow(10, powerReceived(newTrans.getSender(), newTrans.getReceiver())) / interference);
    }

    private void stepForward(double x) {
        for (Sensor s : sensorList)
            if (s.getState() == 0) {
                s.setNextTransmission(s.getNextTransmission() - x); // decrementiamo i tempi di attesa per la prossima trasmissione dei pacchetti in coda
            }
        for (Transmission t : transmissionList) {
            t.setRemaining_time(t.getRemainingTime() - x);          // faccio avanzare i tempi di trasmissione di chi sta trasmettendo
        }
        sim_time += x;
    }

    private List<Sensor> transmitterCandidates() {
        List<Sensor> candidates = new ArrayList<>();
        for (Sensor s : sensorList)
            if (s.getState() == 0) {    // supponiamo che possa trasmettere solo chi è nello stato attesa
                candidates.add(s);      //TODO: ricordiamo che ogni transmitter ha almeno un receiver
            }
        return candidates;
    }

    private List<Sensor> findNeighbors(Sensor s1) {
        List<Sensor> neighbors = new ArrayList<>();
        for (Sensor s2 : sensorList)
            if (s2 != s1 && eucDistance(s1, s2) <= s1.getDistance())  //formula calcolo massima distanza di trasmissione
                neighbors.add(s2);

        return neighbors;
    }

    private void setNeighbors() {
        for (Sensor s : sensorList)
            s.setNeighbors(findNeighbors(s));
    }

    public void mediumDistribution() {
        int cont = 0;
        Sensor min = null;
        int max = -1;
        for (Sensor s : sensorList) {
            min = (min == null || (s.getNeighbors().size() < min.getNeighbors().size())) ? s : min;
            max = s.getNeighbors().size() > max ? s.getNeighbors().size() : max;
            cont += s.getNeighbors().size();
        }
        System.out.println("min : " + min.getNeighbors().size() + " max : " + max + "  med: " + cont / sensorList.size());
    }

    public int numberDisconnected() {
        int cont = 0;
        for (Sensor s : sensorList) {
            cont += s.getNeighbors().isEmpty() ? 1 : 0;
        }
        return cont;
    }

    private double powerReceived(Sensor sender, Sensor receiver) {
        return sender.getPower() - (20 * log10(Forest.D0) + 20 * log10(0.9 * pow(10, 9)) - 147.55) - 35 * log10(eucDistance(sender, receiver) / Forest.D0);
    }

    private double eucDistance(Sensor s1, Sensor s2) {
        return sqrt(pow(s1.getX_position() - s2.getX_position(), 2) + pow(s1.getY_position() - s2.getY_position(), 2));
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


    public void drawSensor(Sensor s) {
        if (Forest.GRAPHICS) {
            draw.setPenRadius(0.002);
            draw.setPenColor(Color.BLACK);
            draw.filledSquare(s.getX_position() / 10, s.getY_position() / 10, pixel_shape);
            draw.circle(s.getX_position() / 10, s.getY_position() / 10, (int) s.getDistance() / 10);
        }
    }

    /**
     * @param s     sensore al quale cambiare colore
     * @param shape colore del cerchio che indica il raggio di trasmissione
     * @param point colore del punto che indica il sensore
     */
    private void colorSensor(Sensor s, Color shape, Color point) {
        if (Forest.GRAPHICS) {
            int x = s.getX_position() / 10;
            int y = s.getY_position() / 10;
            draw.setPenColor(shape);
            draw.circle(x, y, (int) s.getDistance() / 10);
            draw.setPenColor(point);
            draw.filledSquare(x, y, pixel_shape);
        }
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
        if (Forest.GRAPHICS) draw.show();
    }

    void show(int i) {
        if (Forest.GRAPHICS) draw.show(i);
    }
}