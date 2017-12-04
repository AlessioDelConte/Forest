public class Transmission {
    private boolean state;
    private double remaining_time;
    private Sensor sender;
    private Sensor receiver;

    public Transmission(Sensor sender, Sensor receiver, double mu) {
        this.state = true;
        this.remaining_time = Forest.exp(mu);
        this.sender = sender;
        this.receiver = receiver;
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
}
