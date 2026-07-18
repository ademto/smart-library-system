package model;

public interface Borrowable {
    boolean borrowItem(UserAccount user);
    boolean returnItem(UserAccount user);
}
