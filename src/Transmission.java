public class Transmission implements Comparable<Transmission>{
    private boolean state; // true per adesso non ci sono state interferenze false altrimenti
    private Double remaining_time;
    private Double totalTime;
    private Sensor sender;
    private Sensor receiver;

    Transmission(Sensor sender, Sensor receiver, double remaining_time) {
        this.state = true;
        this.remaining_time = remaining_time;
        this.sender = sender;
        this.receiver = receiver;
        this.totalTime = remaining_time;
        sender.setState(1);
        receiver.setState(2);
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setRemaining_time(double remaining_time) {
        this.remaining_time = remaining_time;
    }

    public boolean isState() {
        return state;
    }

    public double getRemainingTime() {
        return remaining_time;
    }

    public Sensor getSender() {
        return sender;
    }

    public Sensor getReceiver() {
        return receiver;
    }

    @Override
    public int compareTo(Transmission o) {
        return remaining_time.compareTo(o.getRemainingTime());
    }

    public Double getTotalTime() {
        return totalTime;
    }
}
