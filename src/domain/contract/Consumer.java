package domain.contract;

// I use this interface to define a stoppable consumer.
public interface Consumer extends Runnable {
    void stop();

    String getName();
}
