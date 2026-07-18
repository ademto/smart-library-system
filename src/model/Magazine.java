package model;

public class Magazine extends LibraryItem {
    public Magazine(String id, String title, String author, int year) {
        super(id, title, author, year);
    }

    public Magazine(String id, String title, String author, int year,
                    boolean available, int accessCount) {
        super(id, title, author, year, available, accessCount);
    }
}
