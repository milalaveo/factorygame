package shared;

// class to hold a run configuration for the factory simulation
public final class FactoryConfig {
    private final int producerCount;
    private final int consumerCount;
    private final int bufferCapacity;
    private final int producerMinDelayMs;
    private final int producerMaxDelayMs;

    public FactoryConfig(
            int producerCount,
            int consumerCount,
            int bufferCapacity,
            int producerMinDelayMs,
            int producerMaxDelayMs
    ) {
        Validation.requirePositive(producerCount, "producerCount");
        Validation.requirePositive(consumerCount, "consumerCount");
        Validation.requirePositive(bufferCapacity, "bufferCapacity");
        Validation.requirePositive(producerMinDelayMs, "producerMinDelayMs");
        Validation.requirePositive(producerMaxDelayMs, "producerMaxDelayMs");
        if (producerMinDelayMs > producerMaxDelayMs) {
            throw new IllegalArgumentException("producerMinDelayMs must be <= producerMaxDelayMs");
        }
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
        this.bufferCapacity = bufferCapacity;
        this.producerMinDelayMs = producerMinDelayMs;
        this.producerMaxDelayMs = producerMaxDelayMs;
    }

    public int getProducerCount() {
        return producerCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public int getProducerMinDelayMs() {
        return producerMinDelayMs;
    }

    public int getProducerMaxDelayMs() {
        return producerMaxDelayMs;
    }
}
