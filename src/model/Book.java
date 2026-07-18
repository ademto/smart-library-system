package model;

public class Book extends LibraryItem {
    public Book(String id, String title, String author, int year) {
        super(id, title, author, year);
    }

    public Book(String id, String title, String author, int year,
                boolean available, int accessCount) {
        super(id, title, author, year, available, accessCount);
    }
}
