package gui;

import controller.LibraryDatabase;
import controller.SearchEngine;
import controller.SortEngine;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.LibraryItem;
import utils.Validation;

public class SearchPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final DefaultTableModel resultModel;

    public SearchPanel(LibraryDatabase database, Runnable onChanged) {
        SearchEngine searchEngine = new SearchEngine(database);
        SortEngine sortEngine = new SortEngine(database);
        setLayout(new BorderLayout(8, 8));

        JPanel controls = new JPanel(new BorderLayout(4, 4));

        JPanel searchArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField queryField = new JTextField(18);
        JComboBox<String> searchFieldBox = new JComboBox<>(
                new String[]{"Title", "Author", "Type"});
        JComboBox<String> searchAlgorithmBox = new JComboBox<>(
                new String[]{"Linear Search", "Binary Search", "Recursive Search"});
        JButton searchButton = new JButton("Search");
        JLabel typingLabel = new JLabel(" ");

        searchArea.add(new JLabel("Search by"));
        searchArea.add(searchFieldBox);
        searchArea.add(queryField);
        searchArea.add(searchAlgorithmBox);
        searchArea.add(searchButton);
        searchArea.add(typingLabel);

        JPanel sortArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> sortFieldBox = new JComboBox<>(
                new String[]{"Title", "Author", "Year", "Type"});
        JComboBox<String> sortAlgorithmBox = new JComboBox<>(
                new String[]{"Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"});
        JButton sortButton = new JButton("Sort Items");

        sortArea.add(new JLabel("Sort by"));
        sortArea.add(sortFieldBox);
        sortArea.add(new JLabel("using"));
        sortArea.add(sortAlgorithmBox);
        sortArea.add(sortButton);

        controls.add(searchArea, BorderLayout.NORTH);
        controls.add(sortArea, BorderLayout.SOUTH);
        add(controls, BorderLayout.NORTH);

        resultModel = new DefaultTableModel(
                new Object[]{"ID", "Type", "Title", "Author", "Year", "Available"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable resultTable = new JTable(resultModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        queryField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                typingLabel.setText(queryField.getText().isBlank()
                        ? " " : "Searching for: " + queryField.getText().trim());
            }

            @Override
            public void insertUpdate(DocumentEvent event) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                update();
            }
        });

        searchButton.addActionListener(event -> {
            String query = queryField.getText().trim();
            if (!Validation.isNonBlank(query)) {
                JOptionPane.showMessageDialog(this, "Enter a search value.",
                        "Missing input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SearchEngine.SearchField field = toSearchField(
                    (String) searchFieldBox.getSelectedItem());
            ArrayList<LibraryItem> results;
            int selectedAlgorithm = searchAlgorithmBox.getSelectedIndex();

            if (selectedAlgorithm == 1) {
                // Binary search requires the collection to be sorted by the same field.
                sortEngine.sort(toSortField(field), SortEngine.SortAlgorithm.INSERTION);
                results = searchEngine.binarySearch(query, field);
            } else if (selectedAlgorithm == 2) {
                results = searchEngine.recursiveSearch(query, field);
            } else {
                results = searchEngine.linearSearch(query, field);
            }

            displayResults(results);
            onChanged.run();
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching item was found.");
            }
        });

        sortButton.addActionListener(event -> {
            SortEngine.SortField field = toSortField(
                    (String) sortFieldBox.getSelectedItem());
            SortEngine.SortAlgorithm algorithm = switch (sortAlgorithmBox.getSelectedIndex()) {
                case 1 -> SortEngine.SortAlgorithm.INSERTION;
                case 2 -> SortEngine.SortAlgorithm.MERGE;
                case 3 -> SortEngine.SortAlgorithm.QUICK;
                default -> SortEngine.SortAlgorithm.SELECTION;
            };
            sortEngine.sort(field, algorithm);
            onChanged.run();
            JOptionPane.showMessageDialog(this,
                    "Items sorted by " + sortFieldBox.getSelectedItem()
                            + " using " + sortAlgorithmBox.getSelectedItem() + ".");
        });
    }

    private void displayResults(ArrayList<LibraryItem> results) {
        resultModel.setRowCount(0);
        for (LibraryItem item : results) {
            resultModel.addRow(new Object[]{
                    item.getId(), item.getClass().getSimpleName(), item.getTitle(),
                    item.getAuthor(), item.getYear(), item.isAvailable()
            });
        }
    }

    private SearchEngine.SearchField toSearchField(String value) {
        return switch (value) {
            case "Author" -> SearchEngine.SearchField.AUTHOR;
            case "Type" -> SearchEngine.SearchField.TYPE;
            default -> SearchEngine.SearchField.TITLE;
        };
    }

    private SortEngine.SortField toSortField(SearchEngine.SearchField field) {
        return switch (field) {
            case AUTHOR -> SortEngine.SortField.AUTHOR;
            case TYPE -> SortEngine.SortField.TYPE;
            case TITLE -> SortEngine.SortField.TITLE;
        };
    }

    private SortEngine.SortField toSortField(String value) {
        return switch (value) {
            case "Author" -> SortEngine.SortField.AUTHOR;
            case "Year" -> SortEngine.SortField.YEAR;
            case "Type" -> SortEngine.SortField.TYPE;
            default -> SortEngine.SortField.TITLE;
        };
    }
}
