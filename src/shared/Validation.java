package shared;

// helpers for simple validation
public final class Validation {
    private Validation() {
    }

    public static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
    }
}
