package model;

public abstract class LibraryItem implements Borrowable {
    private final String id;
    private String title;
    private String author;
    private int year;
    private boolean available;
    private int accessCount;

    public LibraryItem(String id, String title, String author, int year) {
        this(id, title, author, year, true, 0);
    }

    public LibraryItem(String id, String title, String author, int year,
                       boolean available, int accessCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = available;
        this.accessCount = Math.max(0, accessCount);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = Math.max(0, accessCount);
    }

    public void recordAccess() {
        accessCount++;
    }

    @Override
    public boolean borrowItem(UserAccount user) {
        if (!available) {
            return false;
        }
        available = false;
        return true;
    }

    @Override
    public boolean returnItem(UserAccount user) {
        if (available) {
            return false;
        }
        available = true;
        return true;
    }

    @Override
    public String toString() {
        return id + " - " + title + " (" + getClass().getSimpleName() + ")";
    }
}
