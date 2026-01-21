package shared;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// helper to format timestamps for logs
public final class TimeFormatter {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private TimeFormatter() {
    }

    public static String format(Instant timestamp) {
        return FORMATTER.format(timestamp);
    }
}
