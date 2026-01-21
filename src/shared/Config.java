package shared;

// default config values
public final class Config {
    public static final int RESOURCE_POINTS = 10;
    public static final int BASE_PRODUCER_COUNT = 1;
    public static final int BASE_CONSUMER_COUNT = 1;
    public static final int BASE_BUFFER_CAPACITY = 4;
    public static final int PRODUCER_MIN_DELAY_MS = 300;
    public static final int PRODUCER_MAX_DELAY_MS = 900;
    public static final int GOAL_PROCESSED = 25;

    private Config() {
    }
}
