public class Transmission implements Comparable<Transmission>{
    private boolean state;
    private Double remaining_time;
    private Sensor sender;
    private Sensor receiver;

    public Transmission(Sensor sender, Sensor receiver, double remaining_time) {
        this.state = true;
        this.remaining_time =remaining_time;
        this.sender = sender;
        this.receiver = receiver;
    }

    public void setRemaining_time(double remaining_time) {
        this.remaining_time = remaining_time;
    }

    public boolean isState() {
        return state;
    }

    public double getRemaining_time() {
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
        return remaining_time.compareTo(o.getRemaining_time());
    }
}
