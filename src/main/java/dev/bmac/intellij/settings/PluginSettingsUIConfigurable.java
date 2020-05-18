package dev.bmac.intellij.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;

/**
 * Settings editor
 **/
public class PluginSettingsUIConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private JPanel myPanel;

    private JCheckBox frontEndIndex;
    private JCheckBox todoIndex;
    private JPanel indexPaths;

    private IndexExclusionTableModel tableModel;
    private JBTable table;
    private List<IndexExclusion> indexExcludeList;

    private PluginSettings pluginSettings = PluginSettings.getInstance();

    public PluginSettingsUIConfigurable() {
        indexExcludeList = pluginSettings.getIndexPathExclude();
        tableModel = new IndexExclusionTableModel(indexExcludeList);
        table = new JBTable(tableModel);
        table.getEmptyText().setText("Add paths to be excluded from indexing");
        TableColumn tableColumn = table.getColumnModel().getColumn(1);
        JTableHeader tableHeader = table.getTableHeader();
        FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());
        int width = headerFontMetrics.stringWidth(table.getColumnName(1)) + 50;
        tableColumn.setMinWidth(width);
        tableColumn.setPreferredWidth(width);

        tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(tableColumn.getMaxWidth());

        indexPaths.setLayout(new BorderLayout());
        indexPaths.add(ToolbarDecorator.createDecorator(table).disableUpDownActions()
                .setEditAction(action -> {
                    editCurrentItem();
                }).setAddAction(action -> {
                    IndexExclusion indexExclusion = new IndexExclusion();
                    IndexExclusionEditor editor = new IndexExclusionEditor(indexExclusion, () -> {
                        indexExcludeList.add(indexExclusion);
                        int i = indexExcludeList.size() - 1;
                        tableModel.fireTableRowsInserted(i, i);
                    });
                    editor.setLocationRelativeTo(getPreferredFocusedComponent());
                    editor.pack();
                    editor.setVisible(true);
                }).setRemoveAction(action -> {
                    int i = table.getSelectedRow();
                    indexExcludeList.remove(i);
                    tableModel.fireTableRowsDeleted(i, i);
                }).createPanel(), BorderLayout.CENTER);

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent event) {
                editCurrentItem();
                return true;
            }
        }.installOn(table);

        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

    }

    private void editCurrentItem() {
        int i = table.getSelectedRow();
        IndexExclusion indexExclusion = indexExcludeList.get(i);
        IndexExclusionEditor editor = new IndexExclusionEditor(indexExclusion, () -> {
            tableModel.fireTableRowsUpdated(i, i);
        });
        editor.setLocationRelativeTo(getPreferredFocusedComponent());
        editor.pack();
        editor.setVisible(true);
    }


    @NotNull
    @Override
    public String getId() {
        return "dev.bmac.intellij.indexExclude";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Index Exclusion";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return myPanel;
    }

    @Override
    public boolean isModified() {
        return pluginSettings.isTODOIndexDisabled() != todoIndex.isSelected() ||
                pluginSettings.isFrontEndIndexDisabled() != frontEndIndex.isSelected() ||
                !pluginSettings.getIndexPathExclude().equals(indexExcludeList);
    }

    @Override
    public void apply() throws ConfigurationException {
        pluginSettings.setFrontEndIndexDisabled(frontEndIndex.isSelected());
        pluginSettings.setTODOIndexDisabled(todoIndex.isSelected());
        pluginSettings.setIndexPathExclude(indexExcludeList);
    }

    private void createUIComponents() {
    }

}
