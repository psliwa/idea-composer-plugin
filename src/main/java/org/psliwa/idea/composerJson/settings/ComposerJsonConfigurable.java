package org.psliwa.idea.composerJson.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ComposerJsonConfigurable implements Configurable {

    private final Project project;

    private JComponent component;
    private JPanel unboundedVersionPanel;
    private JPanel customReposPanel;

    private List<TabularConfiguration<?>> configurations = new LinkedList<TabularConfiguration<?>>();

    public ComposerJsonConfigurable(@NotNull Project project) {
        this.project = project;

        ListTableModel<PatternItem> unboundVersionsModel = new ListTableModel<PatternItem>(
            new PatternColumn[]{new PatternColumn()},
            new ArrayList<PatternItem>()
        );

        ListTableModel<EnabledItem> customReposModel = new ListTableModel<EnabledItem>(
            new ColumnInfo[]{ new EnabledNameColumn(), new EnableColumn() },
            new ArrayList<EnabledItem>()
        );

        configurations.add(new TabularConfiguration<PatternItem>(
            new LazyRef<JPanel>() {
                @Override
                public JPanel get() {
                    return unboundedVersionPanel;
                }
            },
            unboundVersionsModel,
            getComposerJsonSettings().getUnboundedVersionInspectionSettings()
        ) {
            @Override
            JComponent createComponent() {
                TableView<PatternItem> tableView = createTableView();

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

                return tableDecorator.createPanel();
            }
        });

        configurations.add(new TabularConfiguration<EnabledItem>(
            new LazyRef<JPanel>() {
                @Override
                public JPanel get() {
                    return customReposPanel;
                }
            },
            customReposModel,
            getComposerJsonSettings().getCustomRepositoriesSettings()
        ) {
            @Override
            JComponent createComponent() {
                TableView<EnabledItem> tableView = createTableView();

                ToolbarDecorator tableDecorator = ToolbarDecorator.createDecorator(tableView);

                tableDecorator.disableUpAction();
                tableDecorator.disableDownAction();

                return tableDecorator.createPanel();
            }
        });
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

    private ComposerJsonSettings getComposerJsonSettings() {
        return ComposerJsonSettings.getInstance(project);
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        for(TabularConfiguration<?> config : configurations) {
            config.attachComponent();
        }

        return component;
    }

    @Override
    public boolean isModified() {
        for(TabularConfiguration<?> config : configurations) {
            if(config.isModified()) return true;
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        for(TabularConfiguration<?> config : configurations) {
            config.apply();
        }
    }

    @Override
    public void reset() {
        for(TabularConfiguration<?> config : configurations) {
            config.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        for(TabularConfiguration<?> config : configurations) {
            config.disposeUIResources();
        }
    }

    private static class EnabledNameColumn extends ColumnInfo<EnabledItem, String> {
        public EnabledNameColumn() {
            super("Name");
        }

        @Nullable
        @Override
        public String valueOf(EnabledItem enabledItem) {
            return enabledItem.getName();
        }

        @Override
        public boolean isCellEditable(EnabledItem enabledItem) {
            return false;
        }
    }

    private static class EnableColumn extends ColumnInfo<EnabledItem, Boolean> {
        private TableCellRenderer renderer = new BooleanTableCellRenderer();
        private TableCellEditor editor = new BooleanTableCellEditor();

        public EnableColumn() {
            super("Enabled");
        }

        @Nullable
        @Override
        public Boolean valueOf(EnabledItem enabledItem) {
            return enabledItem.isEnabled();
        }

        @Override
        public boolean isCellEditable(EnabledItem enabledItem) {
            return true;
        }

        @Override
        public void setValue(EnabledItem enabledItem, Boolean value) {
            enabledItem.setEnabled(value);
        }

        @Nullable
        @Override
        public TableCellRenderer getRenderer(EnabledItem enabledItem) {
            return renderer;
        }

        @Nullable
        @Override
        public TableCellEditor getEditor(EnabledItem enabledItem) {
            return editor;
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

    private abstract static class TabularConfiguration<Model> {
        private final LazyRef<JPanel> component;
        private final ListTableModel<Model> model;
        private final TabularSettings<Model> settings;

        private TabularConfiguration(LazyRef<JPanel> component, ListTableModel<Model> model, TabularSettings<Model> settings) {
            this.component = component;
            this.model = model;
            this.settings = settings;
        }

        void apply() {
            List<Model> patternItems = model.getItems();
            settings.setValues(patternItems);
        }

        void reset() {
            List<Model> items = new ArrayList<Model>();
            items.addAll(settings.getValues());

            model.setItems(items);
        }

        void disposeUIResources() {
            while(model.getRowCount() > 0) {
                model.removeRow(0);
            }
        }

        TableView<Model> createTableView() {
            TableView<Model> tableView = new TableView<Model>();
            tableView.setModelAndUpdateColumns(model);

            return tableView;
        }

        boolean isModified() {
            return !model.getItems().equals(settings.getValues());
        }

        void attachComponent() {
            component.get().add(createComponent());
        }

        abstract JComponent createComponent();
    }

    private interface LazyRef<T> {
        T get();
    }
}
