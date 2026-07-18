package model;

public class Journal extends LibraryItem {
    public Journal(String id, String title, String author, int year) {
        super(id, title, author, year);
    }

    public Journal(String id, String title, String author, int year,
                   boolean available, int accessCount) {
        super(id, title, author, year, available, accessCount);
    }
}
