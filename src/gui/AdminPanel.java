package gui;

import controller.LibraryDatabase;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;
import utils.IDGenerator;
import utils.Validation;

public class AdminPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public AdminPanel(LibraryDatabase database, Runnable onChanged) {
        setLayout(new GridBagLayout());
        GridBagConstraints outer = new GridBagConstraints();
        outer.insets = new Insets(8, 8, 8, 8);
        outer.fill = GridBagConstraints.HORIZONTAL;
        outer.weightx = 1;

        JComboBox<String> operationBox = new JComboBox<>(new String[]{
                "Add Library Item", "Add User", "Remove / Undo Item"
        });
        operationBox.setToolTipText("Choose the administrative operation to display");

        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);
        cards.add(createAddItemCard(database, onChanged), "Add Library Item");
        cards.add(createAddUserCard(database, onChanged), "Add User");
        cards.add(createRemoveCard(database, onChanged), "Remove / Undo Item");

        operationBox.addActionListener(event ->
                cardLayout.show(cards, (String) operationBox.getSelectedItem()));

        outer.gridx = 0;
        outer.gridy = 0;
        add(operationBox, outer);
        outer.gridy = 1;
        outer.fill = GridBagConstraints.BOTH;
        outer.weighty = 1;
        add(cards, outer);
    }

    private JPanel createAddItemCard(LibraryDatabase database, Runnable onChanged) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = constraints();

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
        JTextField titleField = new JTextField(22);
        JTextField authorField = new JTextField(22);
        JTextField yearField = new JTextField(22);
        JButton addButton = new JButton("Add Item");
        addButton.setMnemonic('I');
        addButton.setToolTipText("Add a new book, magazine, or journal");

        addRow(panel, "Type", typeBox, 0, gbc);
        addRow(panel, "Title", titleField, 1, gbc);
        addRow(panel, "Author", authorField, 2, gbc);
        addRow(panel, "Year", yearField, 3, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);

        addButton.addActionListener(event -> {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                if (!Validation.isNonBlank(title)
                        || !Validation.isNonBlank(author)
                        || !Validation.isValidYear(year)) {
                    throw new IllegalArgumentException(
                            "Enter a valid title, author, and publication year.");
                }

                String id = IDGenerator.generateItemId();
                String type = (String) typeBox.getSelectedItem();
                LibraryItem item = switch (type) {
                    case "Magazine" -> new Magazine(id, title, author, year);
                    case "Journal" -> new Journal(id, title, author, year);
                    default -> new Book(id, title, author, year);
                };

                database.addItem(item);
                titleField.setText("");
                authorField.setText("");
                yearField.setText("");
                onChanged.run();
                JOptionPane.showMessageDialog(this, "Item added with ID " + id + ".");
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "Year must be a number.",
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(),
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private JPanel createAddUserCard(LibraryDatabase database, Runnable onChanged) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = constraints();

        JTextField nameField = new JTextField(22);
        JTextField emailField = new JTextField(22);
        JButton addButton = new JButton("Add User");
        addButton.setMnemonic('U');

        addRow(panel, "Name", nameField, 0, gbc);
        addRow(panel, "Email", emailField, 1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);

        addButton.addActionListener(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            if (!Validation.isNonBlank(name) || !Validation.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this,
                        "Enter a valid name and email address.",
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserAccount user = new UserAccount(name, email);
            if (!database.addUser(user)) {
                JOptionPane.showMessageDialog(this,
                        "A user with that email address already exists.",
                        "Duplicate user", JOptionPane.WARNING_MESSAGE);
                return;
            }

            nameField.setText("");
            emailField.setText("");
            onChanged.run();
            JOptionPane.showMessageDialog(this,
                    "User added with ID " + user.getUserId() + ".");
        });
        return panel;
    }

    private JPanel createRemoveCard(LibraryDatabase database, Runnable onChanged) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = constraints();

        JTextField itemIdField = new JTextField(22);
        JButton removeButton = new JButton("Remove Item");
        JButton undoButton = new JButton("Undo Last Removal");
        removeButton.setMnemonic('R');
        undoButton.setMnemonic('Z');

        addRow(panel, "Item ID", itemIdField, 0, gbc);
        JPanel buttons = new JPanel();
        buttons.add(removeButton);
        buttons.add(undoButton);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(buttons, gbc);

        removeButton.addActionListener(event -> {
            LibraryDatabase.RemovalResult result =
                    database.removeItem(itemIdField.getText().trim());
            String message = switch (result) {
                case REMOVED -> "Item removed. You can restore it with Undo.";
                case BORROWED -> "The item is currently borrowed and cannot be removed.";
                case RESERVED -> "The item has an active reservation queue and cannot be removed.";
                case NOT_FOUND -> "Item not found.";
            };
            if (result == LibraryDatabase.RemovalResult.REMOVED) {
                itemIdField.setText("");
                onChanged.run();
            }
            JOptionPane.showMessageDialog(this, message);
        });

        undoButton.addActionListener(event -> {
            LibraryItem restored = database.undoLastRemoval();
            if (restored != null) {
                onChanged.run();
            }
            JOptionPane.showMessageDialog(this,
                    restored == null ? "Nothing to undo." : restored.getTitle() + " restored.");
        });
        return panel;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addRow(JPanel panel, String label, java.awt.Component component,
                        int row, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }
}
