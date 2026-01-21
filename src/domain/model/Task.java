package domain.model;

import java.time.Instant;

// I use this class to represent a produced task.
public final class Task {
    private final long id;
    private final TaskType type;
    private final Instant createdAt;

    public Task(long id, TaskType type, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public TaskType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getProcessingTimeMs() {
        return type.getProcessingTimeMs();
    }
}
