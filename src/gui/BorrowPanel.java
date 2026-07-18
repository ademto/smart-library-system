package gui;

import controller.BorrowController;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import utils.Validation;

public class BorrowPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public BorrowPanel(BorrowController controller, Runnable onChanged) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(20);
        JTextField itemField = new JTextField(20);
        JButton borrowButton = new JButton("Borrow / Join Waitlist");
        borrowButton.setMnemonic('B');
        borrowButton.setToolTipText("Borrow an available item or join its reservation queue");

        addRow("User ID", userField, 0, gbc);
        addRow("Item ID", itemField, 1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(borrowButton, gbc);

        borrowButton.addActionListener(event -> {
            String userId = userField.getText().trim();
            String itemId = itemField.getText().trim();
            if (!Validation.isNonBlank(userId) || !Validation.isNonBlank(itemId)) {
                JOptionPane.showMessageDialog(this, "Enter both the user ID and item ID.",
                        "Missing input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BorrowController.BorrowResult result = controller.borrowItem(userId, itemId);
            String message = switch (result) {
                case BORROWED -> "Item borrowed successfully.";
                case WAITLISTED -> "Item is unavailable. The user was added to the reservation queue.";
                case ALREADY_WAITING -> "The user is already in this item's reservation queue.";
                case ALREADY_BORROWED -> "The user already borrowed this item.";
                case USER_NOT_FOUND -> "User ID not found.";
                case ITEM_NOT_FOUND -> "Item ID not found.";
            };
            onChanged.run();
            JOptionPane.showMessageDialog(this, message);
        });
    }

    private void addRow(String label, java.awt.Component component,
                        int row, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        add(new JLabel(label), gbc);
        gbc.gridx = 1;
        add(component, gbc);
    }
}
