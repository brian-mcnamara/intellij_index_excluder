package dev.bmac.intellij.settings;

import com.intellij.util.ui.ItemRemovable;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Model for the columns in the index exclusion list
 * Created by brian.mcnamara on Dec 19 2019
 **/
public class IndexExclusionTableModel extends AbstractTableModel implements ItemRemovable {
    private final List<IndexExclusion> indexExclusionList;

    public IndexExclusionTableModel(List<IndexExclusion> indexExclusionList) {
        this.indexExclusionList = indexExclusionList;
    }

    @Override
    public int getRowCount() {
        return indexExclusionList.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0: return "Path";
            case 1: return "Indexer exclusion";
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        IndexExclusion indexExclusion = indexExclusionList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return indexExclusion.getPath();
            case 1:
                return indexExclusion.getIndexerNameExclusion().isDefault() ? "Default" : "Custom";
        }
        return "";
    }

    @Override
    public void removeRow(int idx) {
        indexExclusionList.remove(idx);
    }
}
