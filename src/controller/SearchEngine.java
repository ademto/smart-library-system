package controller;

import java.util.ArrayList;
import java.util.Locale;
import model.LibraryItem;
import model.Reservation;
import model.UserAccount;

public class SearchEngine {
    public enum SearchField {
        TITLE,
        AUTHOR,
        TYPE
    }

    private final LibraryDatabase database;

    public SearchEngine(LibraryDatabase database) {
        this.database = database;
    }

    public LibraryItem findItemById(String itemId) {
        return database.findItemById(itemId);
    }

    public UserAccount findUserById(String userId) {
        return database.findUserById(userId);
    }

    public Reservation findReservation(LibraryItem item) {
        return database.findReservation(item);
    }

    public ArrayList<LibraryItem> linearSearch(String query, SearchField field) {
        ArrayList<LibraryItem> results = new ArrayList<>();
        for (LibraryItem item : database.getItems()) {
            if (matches(item, query, field)) {
                results.add(item);
                database.recordAccess(item);
            }
        }
        return results;
    }

    public ArrayList<LibraryItem> binarySearch(String query, SearchField field) {
        ArrayList<LibraryItem> results = new ArrayList<>();
        for (LibraryItem item : database.getItems()) {
            if (matches(item, query, field)) {
                results.add(item);
                database.recordAccess(item);
            }
        }
        return results;
    }

    public ArrayList<LibraryItem> recursiveSearch(String query, SearchField field) {
        ArrayList<LibraryItem> results = new ArrayList<>();
        recursiveSearch(database.getItems(), query, field, 0, results);
        return results;
    }

    private void recursiveSearch(ArrayList<LibraryItem> items, String query,
            SearchField field, int index,
            ArrayList<LibraryItem> results) {
        if (index >= items.size()) {
            return;
        }

        LibraryItem item = items.get(index);
        if (matches(item, query, field)) {
            results.add(item);
            database.recordAccess(item);
        }
        recursiveSearch(items, query, field, index + 1, results);
    }

    private boolean matches(LibraryItem item, String query, SearchField field) {
        if (item == null || query == null) {
            return false;
        }

        String itemValue = valueOf(item, field);
        String searchValue = query.trim();
        if (searchValue.isBlank()) {
            return false;
        }

        return itemValue != null
                && itemValue.toLowerCase(Locale.ROOT)
                        .contains(searchValue.toLowerCase(Locale.ROOT));
    }

    public String valueOf(LibraryItem item, SearchField field) {
        return switch (field) {
            case AUTHOR -> item.getAuthor();
            case TYPE -> item.getClass().getSimpleName();
            case TITLE -> item.getTitle();
        };
    }

    public LibraryItem linearSearchByTitle(String title) {
        ArrayList<LibraryItem> results = linearSearch(title, SearchField.TITLE);
        return results.isEmpty() ? null : results.get(0);
    }

    public LibraryItem binarySearchByTitle(String title) {
        ArrayList<LibraryItem> results = binarySearch(title, SearchField.TITLE);
        return results.isEmpty() ? null : results.get(0);
    }

    public LibraryItem recursiveSearchByTitle(String title) {
        ArrayList<LibraryItem> results = recursiveSearch(title, SearchField.TITLE);
        return results.isEmpty() ? null : results.get(0);
    }
}
