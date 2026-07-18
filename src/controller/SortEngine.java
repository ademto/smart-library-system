package controller;

import java.util.ArrayList;
import model.LibraryItem;

public class SortEngine {
    public enum SortField {
        TITLE,
        AUTHOR,
        YEAR,
        TYPE
    }

    public enum SortAlgorithm {
        SELECTION,
        INSERTION,
        MERGE,
        QUICK
    }

    private final LibraryDatabase database;

    public SortEngine(LibraryDatabase database) {
        this.database = database;
    }

    public void sort(SortField field, SortAlgorithm algorithm) {
        switch (algorithm) {
            case SELECTION -> selectionSort(field);
            case INSERTION -> insertionSort(field);
            case MERGE -> mergeSort(field);
            case QUICK -> quickSort(field);
        }
    }

    public void selectionSort(SortField field) {
        ArrayList<LibraryItem> items = database.getItems();
        for (int i = 0; i < items.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < items.size(); j++) {
                if (compare(items.get(j), items.get(minIndex), field) < 0) {
                    minIndex = j;
                }
            }
            swap(items, i, minIndex);
        }
    }

    public void insertionSort(SortField field) {
        ArrayList<LibraryItem> items = database.getItems();
        for (int i = 1; i < items.size(); i++) {
            LibraryItem key = items.get(i);
            int j = i - 1;
            while (j >= 0 && compare(items.get(j), key, field) > 0) {
                items.set(j + 1, items.get(j));
                j--;
            }
            items.set(j + 1, key);
        }
    }

    public void mergeSort(SortField field) {
        ArrayList<LibraryItem> items = database.getItems();
        mergeSort(items, 0, items.size() - 1, field);
    }

    private void mergeSort(ArrayList<LibraryItem> items, int left, int right, SortField field) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        mergeSort(items, left, mid, field);
        mergeSort(items, mid + 1, right, field);
        merge(items, left, mid, right, field);
    }

    private void merge(ArrayList<LibraryItem> items, int left, int mid,
                       int right, SortField field) {
        ArrayList<LibraryItem> temp = new ArrayList<>();
        int i = left;
        int j = mid + 1;

        while (i <= mid && j <= right) {
            if (compare(items.get(i), items.get(j), field) <= 0) {
                temp.add(items.get(i++));
            } else {
                temp.add(items.get(j++));
            }
        }
        while (i <= mid) {
            temp.add(items.get(i++));
        }
        while (j <= right) {
            temp.add(items.get(j++));
        }
        for (int k = 0; k < temp.size(); k++) {
            items.set(left + k, temp.get(k));
        }
    }

    public void quickSort(SortField field) {
        ArrayList<LibraryItem> items = database.getItems();
        quickSort(items, 0, items.size() - 1, field);
    }

    private void quickSort(ArrayList<LibraryItem> items, int low,
                           int high, SortField field) {
        if (low < high) {
            int pivotIndex = partition(items, low, high, field);
            quickSort(items, low, pivotIndex - 1, field);
            quickSort(items, pivotIndex + 1, high, field);
        }
    }

    private int partition(ArrayList<LibraryItem> items, int low,
                          int high, SortField field) {
        LibraryItem pivot = items.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (compare(items.get(j), pivot, field) <= 0) {
                swap(items, ++i, j);
            }
        }
        swap(items, i + 1, high);
        return i + 1;
    }

    private int compare(LibraryItem first, LibraryItem second, SortField field) {
        return switch (field) {
            case AUTHOR -> first.getAuthor().compareToIgnoreCase(second.getAuthor());
            case YEAR -> Integer.compare(first.getYear(), second.getYear());
            case TYPE -> first.getClass().getSimpleName()
                    .compareToIgnoreCase(second.getClass().getSimpleName());
            case TITLE -> first.getTitle().compareToIgnoreCase(second.getTitle());
        };
    }

    private void swap(ArrayList<LibraryItem> items, int first, int second) {
        if (first == second) {
            return;
        }
        LibraryItem temp = items.get(first);
        items.set(first, items.get(second));
        items.set(second, temp);
    }

    // Compatibility methods.
    public void selectionSortByTitle() {
        selectionSort(SortField.TITLE);
    }

    public void insertionSortByAuthor() {
        insertionSort(SortField.AUTHOR);
    }

    public void mergeSortByYear() {
        mergeSort(SortField.YEAR);
    }

    public void quickSortByTitle() {
        quickSort(SortField.TITLE);
    }
}
