package utils;

import java.time.Year;

public final class Validation {
    private Validation() {
    }

    public static boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isNonBlank(email)
                && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static boolean isValidYear(int year) {
        return year >= 1000 && year <= Year.now().getValue();
    }

    public static boolean isValidItemId(String id) {
        return isNonBlank(id) && id.matches("(?i)ITM\\d{4,}");
    }

    public static boolean isValidUserId(String id) {
        return isNonBlank(id) && id.matches("(?i)USR\\d{4,}");
    }
}
