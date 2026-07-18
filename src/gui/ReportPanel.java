package gui;

import controller.ReportGenerator;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ReportPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ReportGenerator generator;
    private final JTextArea reportArea;

    public ReportPanel(ReportGenerator generator) {
        this.generator = generator;
        setLayout(new BorderLayout(8, 8));
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setLineWrap(false);

        JButton refreshButton = new JButton("Generate Reports");
        refreshButton.setMnemonic('G');
        refreshButton.addActionListener(event -> refresh());

        add(new JScrollPane(reportArea), BorderLayout.CENTER);
        add(refreshButton, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        reportArea.setText(generator.fullReport());
        reportArea.setCaretPosition(0);
    }
}
