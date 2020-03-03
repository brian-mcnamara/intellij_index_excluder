package dev.bmac.intellij.settings;

import com.intellij.ui.CheckBoxList;
import com.intellij.ui.CheckBoxListListener;
import com.intellij.ui.JBColor;
import com.intellij.util.indexing.FileBasedIndexExtension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Editor dialog to edit the index exclusion list.
 */
public class IndexExclusionEditor extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox inverseSelection;
    private JPanel indexerNameSelector;
    private JTextField path;
    private JLabel deselectAll;
    private CheckBoxList<String> indexerList;
    private IndexExclusion exclusion;

    private List<String> indexerNames = FileBasedIndexExtension.EXTENSION_POINT_NAME.getExtensionList().stream()
            .map(item -> item.getName().getName()).collect(Collectors.toList());

    private final Runnable onOk;

    public IndexExclusionEditor(IndexExclusion exclusion, Runnable onOk) {
        this.onOk = onOk;
        this.exclusion = exclusion;
        this.inverseSelection.setSelected(exclusion.getIndexerNameExclusion().isExcludeIfNotIn());
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        deselectAll.setText("<html><u><a>Deselect all</a></u></html>");
        deselectAll.setForeground(JBColor.BLUE.darker());
        deselectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));

        indexerList = new CheckBoxList<>(new CheckBoxListListener() {
            @Override
            public void checkBoxSelectionChanged(int index, boolean value) {

            }
        });

        indexerList.setItems(indexerNames, null);
        List<String> indexExclusionList = this.exclusion.getIndexerNameExclusion().getIndexerNames();
        this.indexerNames.forEach(i -> {
            if (exclusion.getIndexerNameExclusion().isExcludeIfNotIn()) {
                if (!indexExclusionList.contains(i)) {
                    indexerList.setItemSelected(i, true);
                }
            } else {
                if (indexExclusionList.contains(i)) {
                    indexerList.setItemSelected(i, true);
                }
            }
        });
        this.path.setText(exclusion.getPath());

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        inverseSelection.addItemListener(e -> {
            if(inverseSelection.isSelected()) {
                indexerNames.forEach(i -> indexerList.setItemSelected(i, true));
                repaint();
            }
        });

        deselectAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                indexerNames.forEach(i -> indexerList.setItemSelected(i, false));
                repaint();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        indexerNameSelector.setLayout(new BorderLayout());
        indexerNameSelector.add(new JScrollPane(indexerList));

    }

    private void onOK() {
        exclusion.setPath(path.getText());
        IndexExclusion.IndexNameExclusion indexNameExclusion = exclusion.getIndexerNameExclusion();
        indexNameExclusion.setExcludeIfNotIn(inverseSelection.isSelected());
        indexNameExclusion.setIndexerNames(indexerNames.stream().filter(i -> {
            return inverseSelection.isSelected() && !indexerList.isItemSelected(i) ||
                    !inverseSelection.isSelected() && indexerList.isItemSelected(i);
        }).collect(Collectors.toList()));
        onOk.run();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
