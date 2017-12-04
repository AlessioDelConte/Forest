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


public class Field implements DrawListener {
    private static final double LAMBDA = 3.0;
    private static final double MU = 3.0;
    private static final int SIZE_X = 1500;
    private static final int SIZE_Y = 1000;
    private static final int POWER = -14;
    private static final int SENSIBILITY = -110;

    private static final int pixel_shape = 3;
    private final Point[][] nearest = new Point[SIZE_X][SIZE_Y];  // which point is pixel (i, j) nearest?
    private final Draw draw = new Draw();

    private List<Transmission> trasmissionList;
    private List<Sensor> sensorList;
    private int length;
    private int heigth;
    private double sim_time;

    public Field(int length, int height) {
        draw.setCanvasSize(1440, 820);
        draw.setXscale(0, SIZE_X);
        draw.setYscale(0, SIZE_Y);
        draw.addListener(this);
        draw.clear(Color.GRAY);
        //draw.picture(750, 500, "sfondo-grigio-scuro1.jpg");
        draw.show(20);

        sensorList = new ArrayList<>();
        trasmissionList = new ArrayList<>();
        sim_time = 0.0;
        this.length = length;
        this.heigth = height;

        int id = 0;
        for (int i = 300; i <= length; i += 300)
            for (int j = 303; j <= height; j += 303) {
                sensorList.add(new Sensor(id++, Forest.rand(i - 300, i), Forest.rand(j - 303, j), POWER, SENSIBILITY, LAMBDA));
            }


    }

    public List<Transmission> getTrasmissionList() {
        return trasmissionList;
    }

    void runSimulation() throws InterruptedException {
        Transmission t = null;
        for (int i = 0; i <= 5000; i++) {
            if(!trasmissionList.isEmpty())
                t = Collections.min(trasmissionList);
            Sensor sender = Collections.min(sensorList);

            for(Transmission t1 : trasmissionList)
                System.out.println(t1.getRemaining_time() + "   " + sender.getNext_transmission());

            if (trasmissionList.isEmpty() || sender.getNext_transmission() < t.getRemaining_time()) {
                sim_time += sender.getNext_transmission();
                stepForward(sender.getNext_transmission());
                Sensor receiver = sender.getNeighbors().get(Forest.rand(0, sender.getNeighbors().size()));
                trasmissionList.add(new Transmission(sender, receiver, 0.0005));
                sender.setNext_transmission(Forest.exp(LAMBDA));
                inTransmission(sender);
                inReception(receiver);
            }else{
                System.out.println(1);
                sim_time += t.getRemaining_time();
                endOfTransmission(t.getSender());
                endOfTransmission(t.getReceiver());
                stepForward(t.getRemaining_time());
                trasmissionList.remove(t);
            }

            Thread.sleep(100);
        }
    }

    void stepForward(double x) {
        for (Sensor s : sensorList)
            s.setNext_transmission(s.getNext_transmission() - x);
        for (Transmission t : trasmissionList)
            t.setRemaining_time(t.getRemaining_time()- x);
    }

    List<Sensor> findNeighbors(Sensor s1) {
        List<Sensor> neighbors = new ArrayList<>();
        for (Sensor s2 : sensorList) {
            if (s2 != s1) {
                if (sqrt(pow(s1.getX_position() - s2.getX_position(), 2) + pow(s1.getY_position() - s2.getY_position(), 2)) <= s1.distance())  //formula calcolo massima distanza di trasmissione SPL
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
        //System.out.println("Inserting:       " + p);

        // compare each pixel (i, j) and find nearest point
        //draw.setPenColor(Color.getHSBColor((float) Math.random(), .7f, .7f));
        /*
        for (int i = 0; i < SIZE_X; i++) {
            for (int j = 0; j < SIZE_Y; j++) {
                Point q = new Point(i, j);
                if ((nearest[i][j] == null) || (q.distance(p) < q.distance(nearest[i][j]))) {
                    nearest[i][j] = p;
                    draw.filledSquare(i+0.5, j+0.5, 0.5);
                }
            }
        }*/

        // draw the point afterwards
        draw.setPenRadius(0.003);
        draw.setPenColor(Color.BLACK);
        draw.filledSquare(s.getX_position() / 10, s.getY_position() / 10, pixel_shape);
        draw.circle(s.getX_position() / 10, s.getY_position() / 10, (int) s.distance() / 10);
        //System.out.println("Done processing: " + p);
    }

    public void inTransmission(Sensor s) {
        colorSensorPoint(s, Color.GREEN);
    }

    public void inReception(Sensor s) {
        colorSensorPoint(s, Color.RED);
    }

    public void endOfTransmission(Sensor s) {
        colorSensorPoint(s, Color.BLACK);
    }

    private void colorSensorPoint(Sensor s, Color color) {
        int x = s.getX_position() / 10;
        int y = s.getY_position() / 10;
        draw.setPenColor(color);
        draw.filledSquare(x, y, pixel_shape);
        draw.circle(x, y, (int) s.distance() / 10);
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