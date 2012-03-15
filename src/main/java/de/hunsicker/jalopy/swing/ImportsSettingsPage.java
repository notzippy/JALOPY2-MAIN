/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.ImportPolicy;
import de.hunsicker.swing.EmptyButtonGroup;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer import statement settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public class ImportsSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The delimeter for the value pair of an entry. */
    private static final String DELIMETER_ENTRY_PAIR = ":" /* NOI18N */;
    private static final String STAR = "*" /* NOI18N */;
    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    //~ Instance variables ---------------------------------------------------------------

    /** The grouping entries. */
    DefaultTableModel _tableModel;

    /** Enables/disables import statements collapsing. */
    private JCheckBox _collapseCheckBox;

    /** Enables/disables import statements expanding. */
    private JCheckBox _expandCheckBox;

    /** Enables/disables the grouping functionality. */
    JCheckBox _sortImportsCheckBox;

    /** Specifies the grouping depth. */
    JComboBox _groupingDepthComboBox;

    /** The table which displays the grouping entries. */
    JTable _table;

    /** The table to display the values. */
    TableList _tableList;
    // TODO private boolean _selectionAllowed = true;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ImportsSettingsPage.
     */
    public ImportsSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new ImportsSettingsPage.
     *
     * @param container the parent container.
     */
    ImportsSettingsPage(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void addNotify()
    {
        super.addNotify();

        _table.setDefaultEditor(String.class, new StringEditor(_tableList));
        _table.setDefaultEditor(Integer.class, new IntegerEditor(_tableList));
    }


    /**
     * DOCUMENT ME!
     */
    public void removeNotify()
    {
        super.removeNotify();
        _table.setDefaultEditor(String.class, null);
        _table.setDefaultEditor(Integer.class, null);
    }


    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        if (_expandCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.IMPORT_POLICY, ImportPolicy.EXPAND.getName());
        }
        else if (_collapseCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.IMPORT_POLICY, ImportPolicy.COLLAPSE.getName());
        }
        else
        {
            this.settings.put(
                ConventionKeys.IMPORT_POLICY, ImportPolicy.DISABLED.getName());
        }

        this.settings.putBoolean(
            ConventionKeys.IMPORT_SORT, _sortImportsCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.IMPORT_GROUPING_DEPTH,
            (String) _groupingDepthComboBox.getSelectedItem());

        List values = new ArrayList(_tableModel.getRowCount());

        for (int i = 0; i < _tableModel.getRowCount(); i++)
        {
            String name = (String) _tableModel.getValueAt(i, 0);

            // if the user added a column but never inserted values
            // we ignore the row
            if ((name == null) || (name.length() == 0))
            {
                continue;
            }

            Integer depth = (Integer) _tableModel.getValueAt(i, 1);

            // the user specified only a name
            if (depth == null)
            {
                continue;
            }

            ListEntry entry = new ListEntry(name, depth.toString());
            values.add(entry);
        }

        this.settings.put(ConventionKeys.IMPORT_GROUPING, encodeGroupingInfo(values));
    }


    private JPanel createGeneralPane()
    {
        JPanel generalPanel = new JPanel();

        GridBagLayout generalLayout = new GridBagLayout();
        generalPanel.setLayout(generalLayout);
        generalPanel.setBorder(
            BorderFactory.createTitledBorder(
                this.bundle.getString("BDR_GENERAL" /* NOI18N */)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.top = 0;
        c.insets.bottom = 0;

        _sortImportsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_SORT_IMPORTS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.IMPORT_SORT, ConventionDefaults.IMPORT_SORT));
        _sortImportsCheckBox.addActionListener(this.trigger);

        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
        generalLayout.setConstraints(_sortImportsCheckBox, c);
        generalPanel.add(_sortImportsCheckBox);

        return generalPanel;
    }


    private JPanel createGroupingPane()
    {
        JPanel sortPanel = new JPanel();
        GridBagLayout sortLayout = new GridBagLayout();
        sortPanel.setLayout(sortLayout);
        sortPanel.setBorder(
            BorderFactory.createTitledBorder(
                this.bundle.getString("BDR_ORDERING" /* NOI18N */)));

        GridBagConstraints c = new GridBagConstraints();

        Object[] depths = createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
        NumberComboBoxPanel groupingDepthPanel =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_DEFAULT_GROUPING_DEPTH" /* NOI18N */), depths,
                String.valueOf(
                    this.settings.getInt(
                        ConventionKeys.IMPORT_GROUPING_DEPTH,
                        ConventionDefaults.IMPORT_GROUPING_DEPTH)));
        _groupingDepthComboBox = groupingDepthPanel.getComboBox();
        _groupingDepthComboBox.addActionListener(this.trigger);
        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.top = 0;
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        sortLayout.setConstraints(groupingDepthPanel, c);
        sortPanel.add(groupingDepthPanel);

        List info =
            decodeGroupingInfo(
                this.settings.get(
                    ConventionKeys.IMPORT_GROUPING, ConventionDefaults.IMPORT_GROUPING));
        int rows = info.size();
        Object[][] data = new Object[rows][2];
        int index = 0;

        for (int i = 0, size = info.size(); i < size; i++)
        {
            ListEntry entry = (ListEntry) info.get(i);

            data[index][0] = entry.name;
            data[index][1] = new Integer(entry.depth);
            index++;
        }

        Object[] columnNames =
        {
            this.bundle.getString("HDR_PACKAGE" /* NOI18N */),
            this.bundle.getString("HDR_DEPTH" /* NOI18N */)
        };
        _tableModel = new DataModel(data, columnNames);
        _tableModel.addTableModelListener(
            new TableModelListener()
            {
                public void tableChanged(TableModelEvent ev)
                {
                    // we only handle the DELETE event here, because otherwise
                    // validateSettings() would be called before cell editing
                    // is finished, resulting in error messages
                    if (ev.getType() == TableModelEvent.DELETE)
                    {
                        trigger.actionPerformed(null);
                    }
                }
            });

        _tableList = new TableList(_tableModel, TableList.TYPE_BOTH);
        _table = _tableList.getTable();
        _table.setEnabled(_sortImportsCheckBox.isSelected());

        _sortImportsCheckBox.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // invoked first as the selection listener changes the
                    // state of the buttons too
                    _table.clearSelection();

                    if (_sortImportsCheckBox.isSelected())
                    {
                        _table.setEnabled(true);
                        _tableList.addButton.setEnabled(true);
                        _groupingDepthComboBox.setEnabled(true);
                    }
                    else
                    {
                        _table.setEnabled(false);
                        _tableList.addButton.setEnabled(false);
                        _tableList.removeButton.setEnabled(false);
                        _groupingDepthComboBox.setEnabled(false);
                    }
                }
            });

        ListSelectionListener selectionListener =
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent ev)
                {
                    // ignore extra messages
                    if (ev.getValueIsAdjusting())
                    {
                        return;
                    }

                    ListSelectionModel model = (ListSelectionModel) ev.getSource();
                    int selectedRow = model.getMinSelectionIndex();

                    if (selectedRow > -1)
                    {
                        String name = (String) _tableModel.getValueAt(selectedRow, 0);

                        if (STAR.equals(name))
                        {
                            _tableList.removeButton.setEnabled(false);
                        }
                    }
                }
            };

        updateListenerList(selectionListener);

        JPanel tablePanel = new JPanel();
        GridBagLayout tableLayout = new GridBagLayout();
        tablePanel.setLayout(tableLayout);

        c.insets.left = 0;
        c.insets.right = 0;
        c.insets.top = 10;
        c.insets.bottom = 5;
        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        tableLayout.setConstraints(_tableList, c);
        tablePanel.add(_tableList);

        c.insets.bottom = 2;
        c.insets.top = 10;
        c.insets.left = 10;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 9, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        tableLayout.setConstraints(_tableList.addButton, c);
        tablePanel.add(_tableList.addButton);

        _tableList.removeButton.setEnabled(false);
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        tableLayout.setConstraints(_tableList.removeButton, c);
        tablePanel.add(_tableList.removeButton);

        _tableList.upButton.setEnabled(false);
        c.insets.top = 10;
        c.insets.bottom = 2;
        SwingHelper.setConstraints(
            c, 9, 3, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        tableLayout.setConstraints(_tableList.upButton, c);
        tablePanel.add(_tableList.upButton);

        _tableList.downButton.setEnabled(false);
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 4, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        tableLayout.setConstraints(_tableList.downButton, c);
        tablePanel.add(_tableList.downButton);

        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.top = 0;
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        sortLayout.setConstraints(tablePanel, c);
        sortPanel.add(tablePanel);

        return sortPanel;
    }


    private JPanel createMiscPane()
    {
        JPanel miscPanel = new JPanel();
        GridBagLayout miscLayout = new GridBagLayout();
        miscPanel.setLayout(miscLayout);
        miscPanel.setBorder(
            BorderFactory.createTitledBorder(
                this.bundle.getString("BDR_OPTIMIZE" /* NOI18N */)));

        GridBagConstraints c = new GridBagConstraints();

        ImportPolicy importPolicy =
            ImportPolicy.valueOf(
                this.settings.get(
                    ConventionKeys.IMPORT_POLICY, ConventionDefaults.IMPORT_POLICY));
        _expandCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_EXPAND" /* NOI18N */),
                importPolicy == ImportPolicy.EXPAND);
        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.top = 0;
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscLayout.setConstraints(_expandCheckBox, c);
        miscPanel.add(_expandCheckBox);

        _collapseCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_COLLAPSE" /* NOI18N */),
                importPolicy == ImportPolicy.COLLAPSE);
        c.insets.bottom = 5;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscLayout.setConstraints(_collapseCheckBox, c);
        miscPanel.add(_collapseCheckBox);

        ButtonGroup group = new EmptyButtonGroup();
        group.add(_expandCheckBox);
        group.add(_collapseCheckBox);

        return miscPanel;
    }


    /**
     * Decodes the grouping info found in the given string.
     *
     * @param info string with encoded grouping info.
     *
     * @return a list with the grouping info entries.
     */
    private List decodeGroupingInfo(String info)
    {
        List result = new ArrayList();

        for (
            StringTokenizer tokens = new StringTokenizer(info, DELIMETER);
            tokens.hasMoreElements();)
        {
            String pair = tokens.nextToken();
            int delimOffset = pair.indexOf(':');

            String name = pair.substring(0, delimOffset);
            String depth = pair.substring(delimOffset + 1);

            result.add(new ListEntry(name, depth));
        }

        return result;
    }


    /**
     * Encodes the grouping info found in the given list.
     *
     * @param info list with encoded grouping info.
     *
     * @return a string with the encoded grouping info.
     */
    private String encodeGroupingInfo(List info)
    {
        StringBuffer result = new StringBuffer(60);

        for (int i = 0, size = info.size(); i < size; i++)
        {
            ListEntry entry = (ListEntry) info.get(i);
            result.append(entry.name);
            result.append(DELIMETER_ENTRY_PAIR);
            result.append(entry.depth);
            result.append(DELIMETER);
        }

        if (result.length() > 1)
        {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();

        JPanel generalPanel = createGeneralPane();
        c.insets.left = 0;
        c.insets.right = 0;
        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(generalPanel, c);
        add(generalPanel);

        JPanel sortPanel = createGroupingPane();
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.2,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        layout.setConstraints(sortPanel, c);
        add(sortPanel);

        JPanel miscPanel = createMiscPane();
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(miscPanel, c);
        add(miscPanel);
    }


    /**
     * Updates the selection model of the table list with a custom model.
     *
     * @param selectionListener custom selection listener.
     */
    private void updateListenerList(ListSelectionListener selectionListener)
    {
        DefaultListSelectionModel listSelectionModel =
            (DefaultListSelectionModel) _table.getSelectionModel();

        EventListener[] listeners =
            (EventListener[]) listSelectionModel.getListeners(
                ListSelectionListener.class).clone();

        for (int i = 0; i < listeners.length; i++)
        {
            listSelectionModel.removeListSelectionListener(
                (ListSelectionListener) listeners[i]);
        }

        // add the custom listener first, so it will be notified last
        listSelectionModel.addListSelectionListener(selectionListener);

        for (int i = 0; i < listeners.length; i++)
        {
            listSelectionModel.addListSelectionListener(
                (ListSelectionListener) listeners[i]);
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Provides the data that is to be displayed in the table.
     */
    private static class DataModel
        extends DefaultTableModel
    {
        public DataModel(
            Object[][] data,
            Object[]   columnNames)
        {
            super(data, columnNames);
        }

        public Class getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case 1 :
                    return Integer.class;

                default :
                    return String.class;
            }
        }
    }


    private static final class ListEntry
    {
        String depth;
        String name;

        public ListEntry(
            String name,
            String depth)
        {
            this.name = name;
            this.depth = depth;
        }
    }


    /**
     * Table cell editor for the grouping depth values. Performs input checking.
     */
    private class IntegerEditor
        extends TableList.TableListCellEditor
    {
        public IntegerEditor(TableList tableList)
        {
            super(tableList, new JTextField());
            ((JTextField) getComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        public boolean isCellEditable(EventObject ev)
        {
            boolean editable = super.isCellEditable(ev);

            if (editable)
            {
                // disable editing for the '*' row
                if (STAR.equals(_table.getValueAt(_table.getSelectedRow(), 0)))
                {
                    return false;
                }
            }

            return editable;
        }


        public Object getCellEditorValue()
        {
            String value = (String) super.getCellEditorValue();

            if ((value == null) || (value.trim().length() == 0))
            {
                return EMPTY_STRING;
            }

            return new Integer(value);
        }


        public Component getTableCellEditorComponent(
            JTable  table,
            Object  value,
            boolean isSelected,
            int     row,
            int     column)
        {
            ((JComponent) getComponent()).setBorder(
                BorderFactory.createLineBorder(Color.black));

            return super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        }


        public boolean stopCellEditing()
        {
            String s = (String) super.getCellEditorValue();

            // disallow empty strings
            if (EMPTY_STRING.equals(s))
            {
                ((JComponent) getComponent()).setBorder(
                    BorderFactory.createLineBorder(Color.red));

                return false;
            }

            try
            {
                Integer value = new Integer(s);

                if (value.intValue() < 1)
                {
                    ((JComponent) getComponent()).setBorder(
                        BorderFactory.createLineBorder(Color.red));

                    return false;
                }
            }
            catch (NumberFormatException ex)
            {
                ((JComponent) getComponent()).setBorder(
                    BorderFactory.createLineBorder(Color.red));

                return false;
            }

            boolean successful = super.stopCellEditing();

            if (successful)
            {
                // update the preview
                ImportsSettingsPage.this.trigger.actionPerformed(null);
            }

            return successful;
        }
    }


    /**
     * Table cell editor for the import declaration names. Performs input checking.
     */
    private class StringEditor
        extends TableList.TableListCellEditor
    {
        public StringEditor(TableList tableList)
        {
            super(tableList, new JTextField());
        }

        public boolean isCellEditable(EventObject ev)
        {
            boolean editable = super.isCellEditable(ev);

            if (editable)
            {
                // disable editing for the '*' row
                if (STAR.equals(_table.getValueAt(_table.getSelectedRow(), 0)))
                {
                    return false;
                }
            }

            return editable;
        }


        public Object getCellEditorValue()
        {
            return ((String) super.getCellEditorValue()).trim();
        }


        public Component getTableCellEditorComponent(
            JTable  table,
            Object  value,
            boolean isSelected,
            int     row,
            int     column)
        {
            ((JComponent) getComponent()).setBorder(
                BorderFactory.createLineBorder(Color.black));

            return super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        }


        public boolean stopCellEditing()
        {
            String s = (String) super.getCellEditorValue();

            // only allow package/type names
            if (!Pattern.matches("m/^[a-zA-Z]+(?:.[a-zA-Z]+)*$|\\*/s" /* NOI18N */, s))
            {
                ((JComponent) getComponent()).setBorder(
                    BorderFactory.createLineBorder(Color.red));

                return false;
            }

            int row = _table.getEditingRow();
            int column = _table.getEditingColumn() + 1;
            boolean successful = super.stopCellEditing();

            if (successful)
            {
                // switch to the grouping depth cell to force valid data input
                if (_table.getValueAt(row, column) == null)
                {
                    _table.editCellAt(row, column);

                    DefaultCellEditor cellEditor =
                        (DefaultCellEditor) _table.getCellEditor();
                    Component cell = cellEditor.getComponent();
                    cell.requestFocus();
                }
                else
                {
                    // update the preview
                    ImportsSettingsPage.this.trigger.actionPerformed(null);
                }
            }

            return successful;
        }
    }
}
