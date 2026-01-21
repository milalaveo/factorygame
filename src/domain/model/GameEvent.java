package domain.model;

import java.time.Instant;

// I use this class to pass log events to the UI.
public final class GameEvent {
    private final Instant timestamp;
    private final String actor;
    private final String action;
    private final int bufferSize;

    public GameEvent(Instant timestamp, String actor, String action, int bufferSize) {
        this.timestamp = timestamp;
        this.actor = actor;
        this.action = action;
        this.bufferSize = bufferSize;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
