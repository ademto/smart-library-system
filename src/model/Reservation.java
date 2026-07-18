package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Reservation {
    private final LibraryItem item;
    private final Queue<UserAccount> waitingQueue;

    public Reservation(LibraryItem item) {
        this.item = item;
        this.waitingQueue = new LinkedList<>();
    }

    public boolean addUser(UserAccount user) {
        if (user == null || containsUser(user.getUserId())) {
            return false;
        }
        waitingQueue.offer(user);
        return true;
    }

    public boolean containsUser(String userId) {
        for (UserAccount user : waitingQueue) {
            if (user.getUserId().equalsIgnoreCase(userId)) {
                return true;
            }
        }
        return false;
    }

    public UserAccount serveNextUser() {
        return waitingQueue.poll();
    }

    public UserAccount peekNextUser() {
        return waitingQueue.peek();
    }

    public ArrayList<UserAccount> getWaitingUsers() {
        return new ArrayList<>(waitingQueue);
    }

    public int size() {
        return waitingQueue.size();
    }

    public boolean isEmpty() {
        return waitingQueue.isEmpty();
    }

    public LibraryItem getItem() {
        return item;
    }
}
