package utils;

import controller.LibraryDatabase;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.Reservation;
import model.UserAccount;

public class FileHandler {
    private final Path dataDirectory;

    public FileHandler(String directory) {
        dataDirectory = Paths.get(directory);
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public void save(LibraryDatabase database) throws IOException {
        saveToDirectory(database, dataDirectory);
    }

    public void load(LibraryDatabase database) throws IOException {
        loadFromDirectory(database, dataDirectory, false);
    }

    public void saveToDirectory(LibraryDatabase database, Path directory) throws IOException {
        Files.createDirectories(directory);
        saveItems(database, directory);
        saveUsers(database, directory);
        saveBorrowingHistory(database, directory);
        saveBorrowedItems(database, directory);
        saveReservations(database, directory);
    }

    public void loadFromDirectory(LibraryDatabase database, Path directory,
                                  boolean clearExistingData) throws IOException {
        Files.createDirectories(directory);
        if (clearExistingData) {
            database.clearAll();
        }

        loadItems(database, directory);
        loadUsers(database, directory);
        boolean historyLoaded = loadBorrowingHistory(database, directory);
        loadBorrowedItems(database, directory, historyLoaded);
        loadReservations(database, directory);
    }

    private void saveItems(LibraryDatabase database, Path directory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(directory.resolve("items.txt"))) {
            for (LibraryItem item : database.getItems()) {
                writer.write(String.join("|",
                        item.getClass().getSimpleName(),
                        escape(item.getId()),
                        escape(item.getTitle()),
                        escape(item.getAuthor()),
                        String.valueOf(item.getYear()),
                        String.valueOf(item.isAvailable()),
                        String.valueOf(item.getAccessCount())));
                writer.newLine();
            }
        }
    }

    private void saveUsers(LibraryDatabase database, Path directory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(directory.resolve("users.txt"))) {
            for (UserAccount user : database.getUsers()) {
                writer.write(String.join("|",
                        escape(user.getUserId()),
                        escape(user.getName()),
                        escape(user.getEmail())));
                writer.newLine();
            }
        }
    }

    private void saveBorrowedItems(LibraryDatabase database, Path directory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(directory.resolve("borrowed.txt"))) {
            for (UserAccount user : database.getUsers()) {
                for (LibraryItem item : user.getBorrowedItems()) {
                    LocalDate dueDate = user.getDueDate(item);
                    writer.write(String.join("|",
                            escape(user.getUserId()),
                            escape(item.getId()),
                            dueDate == null ? "" : dueDate.toString()));
                    writer.newLine();
                }
            }
        }
    }

    private void saveBorrowingHistory(LibraryDatabase database, Path directory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(directory.resolve("history.txt"))) {
            for (UserAccount user : database.getUsers()) {
                for (LibraryItem item : user.getBorrowingHistory()) {
                    writer.write(String.join("|",
                            escape(user.getUserId()),
                            item.getClass().getSimpleName(),
                            escape(item.getId()),
                            escape(item.getTitle()),
                            escape(item.getAuthor()),
                            String.valueOf(item.getYear()),
                            String.valueOf(item.getAccessCount())));
                    writer.newLine();
                }
            }
        }
    }

    private void saveReservations(LibraryDatabase database, Path directory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(directory.resolve("reservations.txt"))) {
            for (Reservation reservation : database.getReservations()) {
                for (UserAccount user : reservation.getWaitingUsers()) {
                    writer.write(String.join("|",
                            escape(reservation.getItem().getId()),
                            escape(user.getUserId())));
                    writer.newLine();
                }
            }
        }
    }

    private void loadItems(LibraryDatabase database, Path directory) throws IOException {
        Path file = directory.resolve("items.txt");
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) {
                    continue;
                }

                String type = parts[0];
                String id = unescape(parts[1]);
                String title = unescape(parts[2]);
                String author = unescape(parts[3]);
                int year = Integer.parseInt(parts[4]);
                boolean available = Boolean.parseBoolean(parts[5]);
                int accessCount = parts.length >= 7 ? parseInt(parts[6], 0) : 0;

                LibraryItem item = createItem(type, id, title, author, year,
                        available, accessCount);
                if (item != null) {
                    database.addItem(item);
                    IDGenerator.registerExistingItemId(id);
                }
            }
        }
    }

    private void loadUsers(LibraryDatabase database, Path directory) throws IOException {
        Path file = directory.resolve("users.txt");
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 3) {
                    continue;
                }
                String id = unescape(parts[0]);
                database.addUser(new UserAccount(id, unescape(parts[1]), unescape(parts[2])));
                IDGenerator.registerExistingUserId(id);
            }
        }
    }

    private boolean loadBorrowingHistory(LibraryDatabase database, Path directory)
            throws IOException {
        Path file = directory.resolve("history.txt");
        if (!Files.exists(file)) {
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) {
                    continue;
                }

                UserAccount user = database.findUserById(unescape(parts[0]));
                String type = parts[1];
                String itemId = unescape(parts[2]);
                LibraryItem item = database.findItemById(itemId);

                if (item == null) {
                    int accessCount = parts.length >= 7 ? parseInt(parts[6], 0) : 0;
                    item = createItem(type, itemId, unescape(parts[3]),
                            unescape(parts[4]), parseInt(parts[5], 0), true, accessCount);
                }

                if (user != null && item != null) {
                    user.addLoadedHistoryItem(item);
                }
            }
        }
        return true;
    }

    private void loadBorrowedItems(LibraryDatabase database, Path directory,
                                   boolean historyLoaded) throws IOException {
        Path file = directory.resolve("borrowed.txt");
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 3) {
                    continue;
                }
                UserAccount user = database.findUserById(unescape(parts[0]));
                LibraryItem item = database.findItemById(unescape(parts[1]));
                if (user != null && item != null) {
                    item.setAvailable(false);
                    LocalDate dueDate = parts[2].isBlank()
                            ? LocalDate.now().plusDays(14)
                            : LocalDate.parse(parts[2]);
                    user.borrowLoadedItem(item, dueDate);
                    if (!historyLoaded) {
                        user.addLoadedHistoryItem(item);
                    }
                }
            }
        }
    }

    private void loadReservations(LibraryDatabase database, Path directory)
            throws IOException {
        Path file = directory.resolve("reservations.txt");
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) {
                    continue;
                }
                LibraryItem item = database.findItemById(unescape(parts[0]));
                UserAccount user = database.findUserById(unescape(parts[1]));
                if (item == null || user == null) {
                    continue;
                }

                Reservation reservation = database.findReservation(item);
                if (reservation == null) {
                    reservation = new Reservation(item);
                    database.addReservation(reservation);
                }
                reservation.addUser(user);
            }
        }
    }

    private LibraryItem createItem(String type, String id, String title,
                                   String author, int year, boolean available,
                                   int accessCount) {
        return switch (type) {
            case "Book" -> new Book(id, title, author, year, available, accessCount);
            case "Magazine" -> new Magazine(id, title, author, year, available, accessCount);
            case "Journal" -> new Journal(id, title, author, year, available, accessCount);
            default -> null;
        };
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\b")
                .replace("|", "\\p")
                .replace("\n", "\\n");
    }

    private String unescape(String value) {
        return value.replace("\\n", "\n")
                .replace("\\p", "|")
                .replace("\\b", "\\");
    }
}
