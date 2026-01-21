package domain.contract;

// I use this interface to track stats counters.
public interface GameMetrics {
    void recordProduced();

    void recordProcessed();
}
