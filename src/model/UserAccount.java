package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import utils.IDGenerator;

public class UserAccount {
    private final String userId;
    private String name;
    private String email;
    private final ArrayList<LibraryItem> borrowedItems;
    private final ArrayList<LibraryItem> borrowingHistory;
    private final Map<String, LocalDate> dueDates;

    public UserAccount(String name, String email) {
        this(IDGenerator.generateUserId(), name, email);
    }

    public UserAccount(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.borrowedItems = new ArrayList<>();
        this.borrowingHistory = new ArrayList<>();
        this.dueDates = new HashMap<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<LibraryItem> getBorrowedItems() {
        return borrowedItems;
    }

    public ArrayList<LibraryItem> getBorrowingHistory() {
        return borrowingHistory;
    }

    public boolean hasBorrowedItem(LibraryItem item) {
        return borrowedItems.contains(item);
    }

    public void borrowItem(LibraryItem item) {
        if (!borrowedItems.contains(item)) {
            borrowedItems.add(item);
        }
        borrowingHistory.add(item);
        dueDates.put(item.getId(), LocalDate.now().plusDays(14));
    }

    public void borrowLoadedItem(LibraryItem item, LocalDate dueDate) {
        if (!borrowedItems.contains(item)) {
            borrowedItems.add(item);
        }
        dueDates.put(item.getId(), dueDate);
    }

    public void addLoadedHistoryItem(LibraryItem item) {
        borrowingHistory.add(item);
    }

    public void returnItem(LibraryItem item) {
        borrowedItems.remove(item);
        dueDates.remove(item.getId());
    }

    public LocalDate getDueDate(LibraryItem item) {
        return dueDates.get(item.getId());
    }

    public ArrayList<LibraryItem> getOverdueItems() {
        ArrayList<LibraryItem> overdue = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (LibraryItem item : borrowedItems) {
            LocalDate dueDate = dueDates.get(item.getId());
            if (dueDate != null && dueDate.isBefore(today)) {
                overdue.add(item);
            }
        }
        return overdue;
    }

    public boolean hasOverdueItems() {
        return !getOverdueItems().isEmpty();
    }

    @Override
    public String toString() {
        return userId + " - " + name;
    }
}
