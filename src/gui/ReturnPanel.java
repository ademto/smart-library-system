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

public class ReturnPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public ReturnPanel(BorrowController controller, Runnable onChanged) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(20);
        JTextField itemField = new JTextField(20);
        JButton returnButton = new JButton("Return Item");
        returnButton.setMnemonic('R');

        addRow("User ID", userField, 0, gbc);
        addRow("Item ID", itemField, 1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(returnButton, gbc);

        returnButton.addActionListener(event -> {
            String userId = userField.getText().trim();
            String itemId = itemField.getText().trim();
            if (!Validation.isNonBlank(userId) || !Validation.isNonBlank(itemId)) {
                JOptionPane.showMessageDialog(this, "Enter both the user ID and item ID.",
                        "Missing input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BorrowController.ReturnResult result = controller.returnItem(userId, itemId);
            String message = switch (result) {
                case RETURNED -> "Item returned successfully.";
                case RETURNED_AND_ASSIGNED ->
                        "Item returned and automatically assigned to the next user in the queue.";
                case USER_NOT_FOUND -> "User ID not found.";
                case ITEM_NOT_FOUND -> "Item ID not found.";
                case NOT_BORROWED -> "This user did not borrow the selected item.";
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
