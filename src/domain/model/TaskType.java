package domain.model;

// I use this enum to list the task types.
public enum TaskType {
    CPU(900),
    RAM(600),
    SSD(800),
    GPU(1200);

    private final int processingTimeMs;

    TaskType(int processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public int getProcessingTimeMs() {
        return processingTimeMs;
    }
}
