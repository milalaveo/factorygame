package domain.contract;

// I use this interface to define a stoppable producer.
public interface Producer extends Runnable {
    void stop();

    String getName();
}
