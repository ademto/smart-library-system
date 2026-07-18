package gui;

import controller.BorrowController;
import controller.LibraryDatabase;
import controller.ReportGenerator;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.LibraryItem;
import utils.FileHandler;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String WORKSPACE_CARD = "workspace";
    private static final String CACHE_CARD = "cache";

    private final LibraryDatabase database;
    private final FileHandler fileHandler;
    private final ViewItemsPanel viewItemsPanel;
    private final ReportPanel reportPanel;
    private final JLabel statusBar;
    private final JTextArea cacheArea;
    private final CardLayout mainCardLayout;
    private final JPanel mainCards;

    public MainWindow(LibraryDatabase database, FileHandler fileHandler) {
        super("Smart Library Circulation & Automation System");
        this.database = database;
        this.fileHandler = fileHandler;

        BorrowController borrowController = new BorrowController(database);
        ReportGenerator reportGenerator = new ReportGenerator(database);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        setJMenuBar(createMenuBar());

        statusBar = new JLabel("Ready");
        cacheArea = new JTextArea();
        cacheArea.setEditable(false);

        mainCardLayout = new CardLayout();
        mainCards = new JPanel(mainCardLayout);

        Runnable refresh = this::refreshAll;
        viewItemsPanel = new ViewItemsPanel(database, refresh);
        reportPanel = new ReportPanel(reportGenerator);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("View Items", viewItemsPanel);
        tabs.setMnemonicAt(0, KeyEvent.VK_V);

        JTabbedPane borrowReturnTabs = new JTabbedPane();
        borrowReturnTabs.addTab("Borrow", new BorrowPanel(borrowController, refresh));
        borrowReturnTabs.addTab("Return", new ReturnPanel(borrowController, refresh));
        tabs.addTab("Borrow / Return", borrowReturnTabs);
        tabs.setMnemonicAt(1, KeyEvent.VK_B);

        tabs.addTab("Admin", new AdminPanel(database, refresh));
        tabs.setMnemonicAt(2, KeyEvent.VK_A);
        tabs.addTab("Search & Sort", new SearchPanel(database, refresh));
        tabs.setMnemonicAt(3, KeyEvent.VK_S);
        tabs.addTab("Reports", reportPanel);
        tabs.setMnemonicAt(4, KeyEvent.VK_R);

        tabs.setToolTipTextAt(0, "View all books, magazines, and journals");
        tabs.setToolTipTextAt(1, "Borrow or return an item");
        tabs.setToolTipTextAt(2, "Add users and items or undo a deletion");
        tabs.setToolTipTextAt(3, "Search and sort using student-implemented algorithms");

        mainCards.add(tabs, WORKSPACE_CARD);
        mainCards.add(createCacheCard(), CACHE_CARD);

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton workspaceButton = new JButton("Library Workspace");
        JButton cacheButton = new JButton("Frequent Access Dashboard");
        workspaceButton.setMnemonic('W');
        cacheButton.setMnemonic('F');
        workspaceButton.addActionListener(event ->
                mainCardLayout.show(mainCards, WORKSPACE_CARD));
        cacheButton.addActionListener(event -> {
            refreshCacheArea();
            mainCardLayout.show(mainCards, CACHE_CARD);
        });
        navigation.add(workspaceButton);
        navigation.add(cacheButton);

        add(navigation, BorderLayout.NORTH);
        add(mainCards, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        Timer reminderTimer = new Timer(60_000, event -> updateOverdueStatus());
        reminderTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (saveQuietly()) {
                    dispose();
                    System.exit(0);
                }
            }
        });

        refreshAll();
    }

    private JPanel createCacheCard() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JButton refreshButton = new JButton("Refresh Access Cache");
        refreshButton.addActionListener(event -> refreshCacheArea());
        panel.add(new JScrollPane(cacheArea), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exportItem = new JMenuItem("Export Data...");
        JMenuItem importItem = new JMenuItem("Import Data...");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(event -> {
            if (saveQuietly()) {
                JOptionPane.showMessageDialog(this, "Data saved successfully.");
            }
        });
        exportItem.addActionListener(event -> exportData());
        importItem.addActionListener(event -> importData());
        exitItem.addActionListener(event -> dispatchEvent(
                new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        fileMenu.add(saveItem);
        fileMenu.add(exportItem);
        fileMenu.add(importItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private void exportData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a folder for exported library data");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            Path directory = chooser.getSelectedFile().toPath();
            fileHandler.saveToDirectory(database, directory);
            JOptionPane.showMessageDialog(this,
                    "Data exported to:\n" + directory.toAbsolutePath());
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    "Could not export data: " + exception.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a folder containing exported library data");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Importing will replace the current in-memory data. Continue?",
                "Confirm import", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            fileHandler.loadFromDirectory(database,
                    chooser.getSelectedFile().toPath(), true);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Data imported successfully.");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    "Could not import data: " + exception.getMessage(),
                    "Import error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAll() {
        viewItemsPanel.refresh();
        reportPanel.refresh();
        refreshCacheArea();
        statusBar.setText("Items: " + database.getItems().size()
                + " | Users: " + database.getUsers().size()
                + " | Reservation queues: " + database.getReservations().size());
        saveQuietly();
    }

    private void refreshCacheArea() {
        StringBuilder text = new StringBuilder("MOST FREQUENTLY ACCESSED ITEMS\n\n");
        LibraryItem[] cache = database.getAccessCache();
        if (cache.length == 0) {
            text.append("No item access has been recorded yet.\n");
        } else {
            for (int i = 0; i < cache.length; i++) {
                LibraryItem item = cache[i];
                text.append(i + 1).append(". ")
                        .append(item.getId()).append(" | ")
                        .append(item.getTitle()).append(" | ")
                        .append(item.getClass().getSimpleName()).append(" | Accesses: ")
                        .append(item.getAccessCount()).append('\n');
            }
        }
        cacheArea.setText(text.toString());
    }

    private void updateOverdueStatus() {
        long overdueUsers = database.getUsers().stream()
                .filter(user -> user.hasOverdueItems())
                .count();
        statusBar.setText(overdueUsers > 0
                ? "Overdue reminder: " + overdueUsers + " user(s) have overdue items."
                : "No overdue users. Items: " + database.getItems().size());
    }

    private boolean saveQuietly() {
        try {
            fileHandler.save(database);
            return true;
        } catch (Exception exception) {
            if (isDisplayable()) {
                JOptionPane.showMessageDialog(this,
                        "Could not save data: " + exception.getMessage(),
                        "Save error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }
}
