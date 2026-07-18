package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import model.LibraryItem;
import model.Reservation;
import model.UserAccount;

public class LibraryDatabase {
    public enum RemovalResult {
        REMOVED,
        NOT_FOUND,
        BORROWED,
        RESERVED
    }

    private final ArrayList<LibraryItem> items;
    private final ArrayList<UserAccount> users;
    private final ArrayList<Reservation> reservations;
    private final Stack<LibraryItem> removedItems;

    // Fixed-size array required for the most frequently accessed items cache.
    private final LibraryItem[] accessCache;
    private int cacheSize;

    public LibraryDatabase() {
        items = new ArrayList<>();
        users = new ArrayList<>();
        reservations = new ArrayList<>();
        removedItems = new Stack<>();
        accessCache = new LibraryItem[5];
        cacheSize = 0;
    }

    public boolean addItem(LibraryItem item) {
        if (item == null || findItemById(item.getId()) != null) {
            return false;
        }
        items.add(item);
        rebuildAccessCache();
        return true;
    }

    public RemovalResult removeItem(String itemId) {
        LibraryItem item = findItemById(itemId);
        if (item == null) {
            return RemovalResult.NOT_FOUND;
        }
        if (!item.isAvailable()) {
            return RemovalResult.BORROWED;
        }
        Reservation reservation = findReservation(item);
        if (reservation != null && !reservation.isEmpty()) {
            return RemovalResult.RESERVED;
        }

        items.remove(item);
        removedItems.push(item);
        if (reservation != null) {
            reservations.remove(reservation);
        }
        rebuildAccessCache();
        return RemovalResult.REMOVED;
    }

    public LibraryItem undoLastRemoval() {
        if (removedItems.isEmpty()) {
            return null;
        }
        LibraryItem item = removedItems.pop();
        items.add(item);
        rebuildAccessCache();
        return item;
    }

    public ArrayList<LibraryItem> getItems() {
        return items;
    }

    public boolean addUser(UserAccount user) {
        if (user == null || findUserById(user.getUserId()) != null || findUserByEmail(user.getEmail()) != null) {
            return false;
        }
        users.add(user);
        return true;
    }

    public ArrayList<UserAccount> getUsers() {
        return users;
    }

    public boolean addReservation(Reservation reservation) {
        if (reservation == null || findReservation(reservation.getItem()) != null) {
            return false;
        }
        reservations.add(reservation);
        return true;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
    }

    public LibraryItem findItemById(String itemId) {
        if (itemId == null) {
            return null;
        }
        for (LibraryItem item : items) {
            if (item.getId().equalsIgnoreCase(itemId.trim())) {
                return item;
            }
        }
        return null;
    }

    public UserAccount findUserById(String userId) {
        if (userId == null) {
            return null;
        }
        for (UserAccount user : users) {
            if (user.getUserId().equalsIgnoreCase(userId.trim())) {
                return user;
            }
        }
        return null;
    }

    public UserAccount findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        for (UserAccount user : users) {
            if (user.getEmail().equalsIgnoreCase(email.trim())) {
                return user;
            }
        }
        return null;
    }

    public Reservation findReservation(LibraryItem item) {
        if (item == null) {
            return null;
        }
        for (Reservation reservation : reservations) {
            if (reservation.getItem().getId().equalsIgnoreCase(item.getId())) {
                return reservation;
            }
        }
        return null;
    }

    public void recordAccess(LibraryItem item) {
        if (item == null) {
            return;
        }
        item.recordAccess();
        rebuildAccessCache();
    }

    private void rebuildAccessCache() {
        Arrays.fill(accessCache, null);
        cacheSize = 0;

        for (LibraryItem candidate : items) {
            if (candidate.getAccessCount() <= 0) {
                continue;
            }
            insertIntoCache(candidate);
        }
    }

    private void insertIntoCache(LibraryItem candidate) {
        int insertAt = cacheSize;
        for (int i = 0; i < cacheSize; i++) {
            LibraryItem current = accessCache[i];
            if (candidate.getAccessCount() > current.getAccessCount()
                    || (candidate.getAccessCount() == current.getAccessCount()
                    && candidate.getTitle().compareToIgnoreCase(current.getTitle()) < 0)) {
                insertAt = i;
                break;
            }
        }

        if (insertAt >= accessCache.length) {
            return;
        }

        int lastIndex = Math.min(cacheSize, accessCache.length - 1);
        for (int i = lastIndex; i > insertAt; i--) {
            accessCache[i] = accessCache[i - 1];
        }
        accessCache[insertAt] = candidate;
        if (cacheSize < accessCache.length) {
            cacheSize++;
        }
    }

    public LibraryItem[] getAccessCache() {
        LibraryItem[] result = new LibraryItem[cacheSize];
        System.arraycopy(accessCache, 0, result, 0, cacheSize);
        return result;
    }

    // Explicit polymorphic function: it accepts any LibraryItem subtype.
    public String processLibraryItem(LibraryItem item) {
        if (item == null) {
            return "No item selected.";
        }
        return item.getClass().getSimpleName() + " | " + item.getId() + " | "
                + item.getTitle() + " | " + item.getAuthor() + " | " + item.getYear();
    }

    public void clearAll() {
        items.clear();
        users.clear();
        reservations.clear();
        removedItems.clear();
        Arrays.fill(accessCache, null);
        cacheSize = 0;
    }
}
