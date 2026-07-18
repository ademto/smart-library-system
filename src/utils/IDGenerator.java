package utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class IDGenerator {
    private static final AtomicInteger USER_COUNTER = new AtomicInteger(1);
    private static final AtomicInteger ITEM_COUNTER = new AtomicInteger(1);

    private IDGenerator() {
    }

    public static String generateUserId() {
        return String.format("USR%04d", USER_COUNTER.getAndIncrement());
    }

    public static String generateItemId() {
        return String.format("ITM%04d", ITEM_COUNTER.getAndIncrement());
    }

    public static void registerExistingUserId(String userId) {
        updateCounter(USER_COUNTER, userId, "USR");
    }

    public static void registerExistingItemId(String itemId) {
        updateCounter(ITEM_COUNTER, itemId, "ITM");
    }

    private static void updateCounter(AtomicInteger counter, String id, String prefix) {
        if (id == null || !id.toUpperCase().startsWith(prefix)) {
            return;
        }
        try {
            int number = Integer.parseInt(id.substring(prefix.length()));
            counter.updateAndGet(current -> Math.max(current, number + 1));
        } catch (NumberFormatException ignored) {
            // A manually supplied non-standard ID does not affect automatic numbering.
        }
    }
}
