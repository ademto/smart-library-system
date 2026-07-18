package controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import model.LibraryItem;
import model.UserAccount;

public class ReportGenerator {
    private final LibraryDatabase database;

    public ReportGenerator(LibraryDatabase database) {
        this.database = database;
    }

    public String mostBorrowedItemsReport() {
        Map<String, BorrowStat> counts = new LinkedHashMap<>();
        for (UserAccount user : database.getUsers()) {
            for (LibraryItem item : user.getBorrowingHistory()) {
                BorrowStat stat = counts.get(item.getId());
                if (stat == null) {
                    stat = new BorrowStat(item.getTitle(), item.getClass().getSimpleName());
                    counts.put(item.getId(), stat);
                }
                stat.count++;
            }
        }

        StringBuilder report = new StringBuilder("MOST BORROWED ITEMS\n");
        if (counts.isEmpty()) {
            return report.append("No borrowing history.\n").toString();
        }

        counts.entrySet().stream()
                .sorted((first, second) -> Integer.compare(second.getValue().count, first.getValue().count))
                .forEach(entry -> report.append(entry.getKey())
                        .append(" | ").append(entry.getValue().title)
                        .append(" | ").append(entry.getValue().type)
                        .append(" | ").append(entry.getValue().count)
                        .append(" borrow(s)\n"));
        return report.toString();
    }

    public String overdueUsersReport() {
        StringBuilder report = new StringBuilder("USERS WITH OVERDUE ITEMS\n");
        boolean found = false;
        for (UserAccount user : database.getUsers()) {
            for (LibraryItem item : user.getOverdueItems()) {
                LocalDate dueDate = user.getDueDate(item);
                report.append(user.getUserId()).append(" | ")
                        .append(user.getName()).append(" | ")
                        .append(item.getId()).append(" | ")
                        .append(item.getTitle()).append(" | Due: ")
                        .append(dueDate).append('\n');
                found = true;
            }
        }
        if (!found) {
            report.append("No overdue users.\n");
        }
        return report.toString();
    }

    public String categoryDistributionReport() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (LibraryItem item : database.getItems()) {
            String category = item.getClass().getSimpleName();
            counts.put(category, recursiveCategoryCount(category, 0));
        }

        StringBuilder report = new StringBuilder("CATEGORY DISTRIBUTION\n");
        if (counts.isEmpty()) {
            return report.append("No library items.\n").toString();
        }
        counts.forEach((category, count) -> report.append(category)
                .append(": ").append(count).append('\n'));
        return report.toString();
    }

    private int recursiveCategoryCount(String category, int index) {
        if (index >= database.getItems().size()) {
            return 0;
        }
        LibraryItem item = database.getItems().get(index);
        int current = item.getClass().getSimpleName().equalsIgnoreCase(category) ? 1 : 0;
        return current + recursiveCategoryCount(category, index + 1);
    }

    public String frequentlyAccessedItemsReport() {
        StringBuilder report = new StringBuilder("MOST FREQUENTLY ACCESSED ITEMS\n");
        LibraryItem[] cache = database.getAccessCache();
        if (cache.length == 0) {
            return report.append("No item access has been recorded.\n").toString();
        }
        for (int i = 0; i < cache.length; i++) {
            LibraryItem item = cache[i];
            report.append(i + 1).append(". ")
                    .append(item.getId()).append(" | ")
                    .append(item.getTitle()).append(" | Accesses: ")
                    .append(item.getAccessCount()).append('\n');
        }
        return report.toString();
    }

    public String fullReport() {
        return mostBorrowedItemsReport() + "\n"
                + overdueUsersReport() + "\n"
                + categoryDistributionReport() + "\n"
                + frequentlyAccessedItemsReport();
    }

    private static final class BorrowStat {
        private final String title;
        private final String type;
        private int count;

        private BorrowStat(String title, String type) {
            this.title = title;
            this.type = type;
        }
    }
}
