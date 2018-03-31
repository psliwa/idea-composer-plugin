package org.psliwa.idea.composerJson.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
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
import java.awt.*;
import java.util.*;
import java.util.List;

public class ComposerJsonConfigurable implements Configurable {

    private final Project project;

    private JComponent component;
    private JPanel unboundedVersionPanel;
    private JPanel customReposPanel;
    private JPanel composerUpdateOptionsPanel;

    private List<TabularConfiguration<?>> configurations = new LinkedList<>();

    public ComposerJsonConfigurable(@NotNull Project project) {
        this.project = project;

        ListTableModel<PatternItem> unboundVersionsModel = new ListTableModel<>(
                new PatternColumn[]{new PatternColumn()},
                new ArrayList<>()
        );

        ListTableModel<EnabledItem> customReposModel = new ListTableModel<>(
                new ColumnInfo[]{new EnabledNameColumn(), new EnableColumn()},
                new ArrayList<>()
        );

        ListTableModel<TextItem> composerUpdateOptionsModel = new ListTableModel<>(
                new ColumnInfo[]{new OptionColumn()},
                new ArrayList<>()
        );

        configurations.add(new TabularConfiguration<PatternItem>(
                () -> unboundedVersionPanel,
                unboundVersionsModel,
                getProjectSettings().getUnboundedVersionInspectionSettings()
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
                () -> customReposPanel,
                customReposModel,
                getProjectSettings().getCustomRepositoriesSettings()
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

        configurations.add(new TabularConfiguration<TextItem>(
                () -> composerUpdateOptionsPanel,
                composerUpdateOptionsModel,
                getProjectSettings().getComposerUpdateOptionsSettings()
        ) {
            @Override
            JComponent createComponent() {
                TableView<TextItem> tableView = createTableView();

                ToolbarDecorator tableDecorator = ToolbarDecorator.createDecorator(tableView, new ElementProducer<TextItem>() {
                    @Override
                    public TextItem createElement() {
                        return new TextItem("--");
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

    private ProjectSettings getProjectSettings() {
        return ProjectSettings.getInstance(project);
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        for (TabularConfiguration<?> config : configurations) {
            config.attachComponent();
        }

        return component;
    }

    @Override
    public boolean isModified() {
        for (TabularConfiguration<?> config : configurations) {
            if (config.isModified()) return true;
        }

        return false;
    }

    @Override
    public void apply() {
        for (TabularConfiguration<?> config : configurations) {
            config.apply();
        }
    }

    @Override
    public void reset() {
        for (TabularConfiguration<?> config : configurations) {
            config.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        for (TabularConfiguration<?> config : configurations) {
            config.disposeUIResources();
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        component = new JPanel();
        component.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        component.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setMinimumSize(new Dimension(200, 49));
        panel1.setPreferredSize(new Dimension(200, 49));
        tabbedPane1.addTab(ResourceBundle.getBundle("org/psliwa/idea/composerJson/messages/ComposerBundle").getString("settings.unboundedPackageVersion"), panel1);
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("org/psliwa/idea/composerJson/messages/ComposerBundle").getString("settings.unboundedPackageVersion.label"));
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unboundedVersionPanel = new JPanel();
        unboundedVersionPanel.setLayout(new BorderLayout(0, 0));
        panel1.add(unboundedVersionPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setMinimumSize(new Dimension(200, 49));
        panel2.setPreferredSize(new Dimension(200, 49));
        tabbedPane1.addTab(ResourceBundle.getBundle("org/psliwa/idea/composerJson/messages/ComposerBundle").getString("settings.customRepositories"), panel2);
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("org/psliwa/idea/composerJson/messages/ComposerBundle").getString("settings.customRepositories.label"));
        panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        customReposPanel = new JPanel();
        customReposPanel.setLayout(new BorderLayout(0, 0));
        panel2.add(customReposPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setMinimumSize(new Dimension(200, 49));
        panel3.setPreferredSize(new Dimension(200, 49));
        tabbedPane1.addTab(ResourceBundle.getBundle("org/psliwa/idea/composerJson/messages/ComposerBundle").getString("settings.composerUpdateOptions"), panel3);
        final JLabel label3 = new JLabel();
        label3.setFocusable(true);
        label3.setOpaque(false);
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("org/psliwa/idea/composerJson/messages/ComposerBundle").getString("settings.composerUpdateOptions.label"));
        label3.putClientProperty("html.disable", Boolean.FALSE);
        panel3.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        composerUpdateOptionsPanel = new JPanel();
        composerUpdateOptionsPanel.setLayout(new BorderLayout(0, 0));
        composerUpdateOptionsPanel.putClientProperty("html.disable", Boolean.FALSE);
        panel3.add(composerUpdateOptionsPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return component;
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

    private static class OptionColumn extends ColumnInfo<TextItem, String> {

        public OptionColumn() {
            super("Option");
        }

        @Nullable
        @Override
        public String valueOf(TextItem textItem) {
            return textItem.getText();
        }

        @Override
        public boolean isCellEditable(TextItem textItem) {
            return true;
        }

        @Override
        public void setValue(TextItem textItem, String value) {
            textItem.setText(value);
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
            while (model.getRowCount() > 0) {
                model.removeRow(0);
            }
        }

        TableView<Model> createTableView() {
            TableView<Model> tableView = new TableView<Model>();
            tableView.setModelAndUpdateColumns(model);

            return tableView;
        }

        boolean isModified() {
            return !new HashSet<>(model.getItems()).equals(new HashSet<>(settings.getValues()));
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
