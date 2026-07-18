package gui;

import controller.LibraryDatabase;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.LibraryItem;

public class ViewItemsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryDatabase database;
    private final DefaultTableModel model;
    private final JTable table;
    private final Runnable onChanged;

    public ViewItemsPanel(LibraryDatabase database, Runnable onChanged) {
        this.database = database;
        this.onChanged = onChanged;
        setLayout(new BorderLayout(8, 8));

        model = new DefaultTableModel(
                new Object[]{"ID", "Type", "Title", "Author", "Year", "Available", "Accesses"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setToolTipText("Double-click a row to view the item and record an access");
        table.getColumnModel().getColumn(5).setCellRenderer(new AvailabilityRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showSelectedItem();
                }
            }
        });

        JButton refreshButton = new JButton("Refresh");
        JButton detailsButton = new JButton("View Selected Item");
        refreshButton.addActionListener(event -> refresh());
        detailsButton.addActionListener(event -> showSelectedItem());

        JPanel actions = new JPanel();
        actions.add(detailsButton);
        actions.add(refreshButton);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (LibraryItem item : database.getItems()) {
            model.addRow(new Object[]{
                    item.getId(), item.getClass().getSimpleName(), item.getTitle(),
                    item.getAuthor(), item.getYear(), item.isAvailable(), item.getAccessCount()
            });
        }
    }

    private void showSelectedItem() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        String itemId = String.valueOf(model.getValueAt(modelRow, 0));
        LibraryItem item = database.findItemById(itemId);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "The selected item no longer exists.");
            refresh();
            return;
        }

        database.recordAccess(item);
        JOptionPane.showMessageDialog(this,
                database.processLibraryItem(item)
                        + "\nAvailable: " + item.isAvailable()
                        + "\nAccess count: " + item.getAccessCount(),
                "Library Item", JOptionPane.INFORMATION_MESSAGE);
        refresh();
        onChanged.run();
    }

    private static final class AvailabilityRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused,
                                                       int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            if (!selected) {
                boolean available = Boolean.TRUE.equals(value);
                component.setBackground(available
                        ? new Color(225, 245, 225)
                        : new Color(255, 225, 225));
                component.setForeground(Color.BLACK);
            }
            return component;
        }
    }
}
