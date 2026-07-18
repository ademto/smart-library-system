package controller;

import model.LibraryItem;
import model.Reservation;
import model.UserAccount;

public class BorrowController {
    public enum BorrowResult {
        BORROWED,
        WAITLISTED,
        ALREADY_WAITING,
        ALREADY_BORROWED,
        USER_NOT_FOUND,
        ITEM_NOT_FOUND
    }

    public enum ReturnResult {
        RETURNED,
        RETURNED_AND_ASSIGNED,
        USER_NOT_FOUND,
        ITEM_NOT_FOUND,
        NOT_BORROWED
    }

    private final LibraryDatabase database;
    private final SearchEngine searchEngine;

    public BorrowController(LibraryDatabase database) {
        this.database = database;
        this.searchEngine = new SearchEngine(database);
    }

    public BorrowResult borrowItem(String userId, String itemId) {
        UserAccount user = searchEngine.findUserById(userId);
        if (user == null) {
            return BorrowResult.USER_NOT_FOUND;
        }

        LibraryItem item = searchEngine.findItemById(itemId);
        if (item == null) {
            return BorrowResult.ITEM_NOT_FOUND;
        }

        if (user.hasBorrowedItem(item)) {
            return BorrowResult.ALREADY_BORROWED;
        }

        if (item.borrowItem(user)) {
            user.borrowItem(item);
            database.recordAccess(item);
            return BorrowResult.BORROWED;
        }

        Reservation reservation = searchEngine.findReservation(item);
        if (reservation == null) {
            reservation = new Reservation(item);
            database.addReservation(reservation);
        }

        if (!reservation.addUser(user)) {
            return BorrowResult.ALREADY_WAITING;
        }
        return BorrowResult.WAITLISTED;
    }

    public ReturnResult returnItem(String userId, String itemId) {
        UserAccount user = searchEngine.findUserById(userId);
        if (user == null) {
            return ReturnResult.USER_NOT_FOUND;
        }

        LibraryItem item = searchEngine.findItemById(itemId);
        if (item == null) {
            return ReturnResult.ITEM_NOT_FOUND;
        }

        if (!user.hasBorrowedItem(item)) {
            return ReturnResult.NOT_BORROWED;
        }

        item.returnItem(user);
        user.returnItem(item);
        database.recordAccess(item);

        Reservation reservation = searchEngine.findReservation(item);
        if (reservation != null && !reservation.isEmpty()) {
            UserAccount nextUser = reservation.serveNextUser();
            if (nextUser != null && item.borrowItem(nextUser)) {
                nextUser.borrowItem(item);
            }
            if (reservation.isEmpty()) {
                database.removeReservation(reservation);
            }
            return ReturnResult.RETURNED_AND_ASSIGNED;
        }

        if (reservation != null) {
            database.removeReservation(reservation);
        }
        return ReturnResult.RETURNED;
    }
}
