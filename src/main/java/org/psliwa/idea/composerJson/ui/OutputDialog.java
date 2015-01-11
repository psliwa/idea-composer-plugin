package org.psliwa.idea.composerJson.ui;

import javax.swing.*;

public class OutputDialog extends JDialog {
    private JTextArea outputView;
    private JPanel contentPanel;

    public OutputDialog(String title, String output) {

        setTitle(title);
        setModal(true);

        outputView.setText(title+":\n\n"+output);
        outputView.setCaretPosition(0);
        add(contentPanel);
        pack();

        setLocationRelativeTo(null);
    }
}
