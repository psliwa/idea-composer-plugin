package org.psliwa.idea.composerJson.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChooserDialog<T> extends DialogWrapper {

    private final T[] objects;
    private JPanel chooserPanel;
    private JPanel contentPanel;
    private JLabel titleLabel;
    private final JCheckBox[] checkBoxes;

    public ChooserDialog(@NotNull Project project, @NotNull String title, @NotNull T[] objects) {
        this(project, title, objects, new ToStringPresentation<T>());
    }

    public ChooserDialog(@NotNull Project project, @NotNull String title, @NotNull T[] objects, @NotNull Presentation<T> presentation) {
        super(project);

        this.objects = objects;

        checkBoxes = new JCheckBox[objects.length];
        chooserPanel.setLayout(new GridLayoutManager(objects.length, 1));

        for(int i=0; i<objects.length; i++) {
            JCheckBox checkBox = new JCheckBox(presentation.getPresentation(objects[i]), true);

            checkBoxes[i] = checkBox;
            GridConstraints constraints = new GridConstraints();
            constraints.setRow(i);
            constraints.setAnchor(GridConstraints.ANCHOR_NORTHWEST);
            chooserPanel.add(checkBox, constraints);
        }

        setTitle(title);
        if(title.isEmpty()) {
            titleLabel.setVisible(false);
        } else {
            titleLabel.setText(title+":");
        }
        setOKActionEnabled(true);

        init();
        pack();
    }

    public List<T> showAndGetChosen() {
        if(this.showAndGet()) {
            List<T> list = new LinkedList<T>();

            for(int i=0; i<objects.length; i++) {
                if(checkBoxes[i].isSelected()) {
                    list.add(objects[i]);
                }
            }

            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    public interface Presentation<T> {
        public String getPresentation(T object);
    }

    private static class ToStringPresentation<T> implements Presentation<T> {
        @Override
        public String getPresentation(T object) {
            return object.toString();
        }
    }
}
