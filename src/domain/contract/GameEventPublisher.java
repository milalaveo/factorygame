package domain.contract;

// I use this interface to publish structured events.
public interface GameEventPublisher {
    void publish(String actor, String action, int bufferSize);
}
