import controller.LibraryDatabase;
import gui.MainWindow;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import model.Book;
import model.Journal;
import model.Magazine;
import model.UserAccount;
import utils.FileHandler;
import utils.IDGenerator;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing's default look and feel remains available.
            }

            LibraryDatabase database = new LibraryDatabase();
            Path dataDirectory = Path.of(System.getProperty("user.dir"), "src", "data");
            FileHandler fileHandler = new FileHandler(dataDirectory.toString());

            try {
                fileHandler.load(database);
            } catch (Exception exception) {
                System.err.println("Could not load saved data: " + exception.getMessage());
            }

            if (database.getItems().isEmpty()) {
                database.addItem(new Book(IDGenerator.generateItemId(),
                        "Things Fall Apart", "Chinua Achebe", 1958));
                database.addItem(new Book(IDGenerator.generateItemId(),
                        "Half of a Yellow Sun", "Chimamanda Ngozi Adichie", 2006));
                database.addItem(new Book(IDGenerator.generateItemId(),
                        "The Famished Road", "Ben Okri", 1991));
                database.addItem(new Book(IDGenerator.generateItemId(),
                        "Second Class Citizen", "Buchi Emecheta", 1974));
                database.addItem(new Book(IDGenerator.generateItemId(),
                        "Purple Hibiscus", "Chimamanda Ngozi Adichie", 2003));
                database.addItem(new Magazine(IDGenerator.generateItemId(),
                        "The Guardian Weekend", "Guardian Newspapers", 2024));
                database.addItem(new Journal(IDGenerator.generateItemId(),
                        "Nigerian Journal of Literature", "Nigerian Academy of Letters", 2022));
            }

            if (database.getUsers().isEmpty()) {
                database.addUser(new UserAccount("Emmanuel Adetoro", "emmanuel.adetoro@miva.edu.ng"));
                database.addUser(new UserAccount("Samson Ogundipe", "samson.ogundipe@miva.edu.ng"));
            }

            new MainWindow(database, fileHandler).setVisible(true);
        });
    }
}
