package org.psliwa.idea.composerJson.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ElementProducer;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.psliwa.idea.composerJson.ComposerBundle;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComposerJsonSettingsConfigurable implements Configurable {

    private final Project project;
    private JPanel component;
    private JPanel tablePanel;
    private ListTableModel<PatternItem> modelList = new ListTableModel<PatternItem>(
            new PatternColumn[]{ new PatternColumn() },
            new ArrayList<PatternItem>()
    );
    private boolean changed = false;

    public ComposerJsonSettingsConfigurable(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return ComposerBundle.message("name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        TableView<PatternItem> tableView = new TableView<PatternItem>();
        tableView.setModelAndUpdateColumns(modelList);
        modelList.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                changed = true;
            }
        });

        ToolbarDecorator tableDecorator = ToolbarDecorator.createDecorator(tableView, new ElementProducer<PatternItem>() {
            @Override
            public PatternItem createElement() {
                return new PatternItem("vendor/*");
            }

            @Override
            public boolean canCreateElement() {
                return true;
            }
        });

        tableDecorator.disableUpAction();
        tableDecorator.disableDownAction();

        tablePanel.add(tableDecorator.createPanel());

        return component;
    }

    @Override
    public boolean isModified() {
        return changed;
    }

    @Override
    public void apply() throws ConfigurationException {
        List<PatternItem> items = modelList.getItems();
        getUnboundedVersionInspectionSettings().setExcludedPatterns(items.toArray(new PatternItem[items.size()]));
        changed = false;
    }

    @Override
    public void reset() {
        List<PatternItem> patterns = new ArrayList<PatternItem>();
        Collections.addAll(patterns, getUnboundedVersionInspectionSettings().getExcludedPatterns());

        modelList.setItems(patterns);
        changed = false;
    }

    private ComposerJsonSettings.UnboundedVersionInspectionSettings getUnboundedVersionInspectionSettings() {
        return getSettings().getUnboundedVersionInspectionSettings();
    }

    private ComposerJsonSettings getSettings() {
        return ComposerJsonSettings.getInstance(project);
    }

    @Override
    public void disposeUIResources() {
        while(modelList.getRowCount() > 0) {
            modelList.removeRow(0);
        }
    }

    private static class PatternColumn extends ColumnInfo<PatternItem, String> {

        public PatternColumn() {
            super("Pattern");
        }

        @Nullable
        @Override
        public String valueOf(PatternItem patternItem) {
            return patternItem.getPattern();
        }

        @Override
        public boolean isCellEditable(PatternItem patternItem) {
            return true;
        }

        @Override
        public void setValue(PatternItem patternItem, String value) {
            patternItem.setPattern(value);
        }
    }
}
