/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.hunsicker.jalopy.language.Type;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;


/**
 * A visual, kind of list-like component.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
class TableList
    extends JPanel
{
    //~ Static variables/initializers ----------------------------------------------------

    /** List with ADD/REMOVE buttons. */
    public static final int TYPE_ADD_REMOVE = 1;

    /** List with UP/DOWN buttons. */
    public static final int TYPE_UP_DOWN = 2;

    /** List with ADD/REMOVE and UP/DOWN butttons */
    public static final int TYPE_BOTH = 3;
    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */;
    static final TableCellRenderer RENDERER_DEFAULT = new DefaultRenderer();
    static final TableCellRenderer RENDERER_NUMBER = new NumberRenderer();
    static final TableCellRenderer RENDERER_BOOLEAN = new BooleanRenderer();
    static final TableCellRenderer RENDERER_TYPE = new TypeRenderer();

    //~ Instance variables ---------------------------------------------------------------

    /** Button to add items. */
    JButton addButton;

    /** Button to move an item down the list. */
    JButton downButton;

    /** Button to remove items. */
    JButton removeButton;

    /** Button to move an entry up the list. */
    JButton upButton;

    /** The used table model. */
    DefaultTableModel _tableModel;

    /** The scroller. */
    private JScrollPane _scrollPane;

    /** Used table. */
    JTable _table;

    /** Holds the constraints for the down action. */
    private List _downContraints; // List of <Constraint>

    /** Holds the constraints for the up action. */
    private List _upConstraints; // List of <Constraint>

    /** The list type, i.e. what buttons are available. */
    // todo private int _type;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new TableList object.
     *
     * @param model the table model.
     * @param type the type of the list.
     */
    public TableList(
        DefaultTableModel model,
        int               type)
    {
        _tableModel = model;

        _table = new JTable(_tableModel);
        _table.getTableHeader().setResizingAllowed(false);
        _table.getTableHeader().setReorderingAllowed(false);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setCellSelectionEnabled(false);
        _table.setColumnSelectionAllowed(false);
        _table.setRowSelectionAllowed(true);
        _table.setDefaultRenderer(Object.class, RENDERER_DEFAULT);
        _table.setDefaultRenderer(Number.class, RENDERER_NUMBER);
        _table.setDefaultRenderer(Type.class, RENDERER_TYPE);
        _table.setDefaultRenderer(Boolean.class, RENDERER_BOOLEAN);

        initializeColumnSizes(_table);

        _scrollPane = new JScrollPane(_table);
        _scrollPane.getViewport().setBackground(
            UIManager.getColor("Table.background" /* NOI18N */));

        int height = SwingHelper.getTableHeight(_table);
        _scrollPane.setMinimumSize(new Dimension(300, height));
        _scrollPane.setPreferredSize(new Dimension(300, height + 17));
        _scrollPane.setMaximumSize(new Dimension(300, height + 17));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        layout.setConstraints(_scrollPane, c);
        add(_scrollPane);

        // TODO _type = type;

        switch (type)
        {
            case TYPE_ADD_REMOVE :
                initializeAddRemove();

                break;

            case TYPE_UP_DOWN :
                initializeUpDown();

                break;

            case TYPE_BOTH :
                initializeAddRemove();
                initializeUpDown();

                break;
        }
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the button which can be used to add items to the list.
     *
     * @return button to add items to the list.
     */
    public JButton getAddButton()
    {
        return this.addButton;
    }


    /**
     * Returns the button which can be used to move items down the list.
     *
     * @return button to move item down.
     */
    public JButton getDownButton()
    {
        return this.downButton;
    }


    /**
     * Sets whether or not this component is enabled.
     *
     * @param enable if <code>enable</code> this component will be made enabled (if it
     *        isn't already).
     */
    public void setEnabled(boolean enable)
    {
        if (enable)
        {
            _table.setEnabled(true);
            updateButtons(_table.getRowCount(), _table.getSelectedRow());
        }
        else
        {
            _table.setEnabled(false);
            this.downButton.setEnabled(false);
            this.upButton.setEnabled(false);
        }

        super.setEnabled(enable);
    }


    /**
     * Returns the button which can be used to remove items from the list.
     *
     * @return button to remove items from the the list.
     */
    public JButton getRemoveButton()
    {
        return this.removeButton;
    }


    /**
     * Returns the table.
     *
     * @return table.
     */
    public JTable getTable()
    {
        return _table;
    }


    /**
     * Returns the button which can be used to move items up the list.
     *
     * @return button to move items up.
     */
    public JButton getUpButton()
    {
        return this.upButton;
    }


    /**
     * Adds a constraint for the given types.
     *
     * @param first the first type.
     * @param second the second type.
     * @param constraint the type of the constraint ({@link Constraint#UP} or {@link
     *        Constraint#DOWN}).
     */
    public void addConstraint(
        Type first,
        Type second,
        int  constraint)
    {
        if (constraint == Constraint.UP)
        {
            if (_upConstraints == null)
            {
                _upConstraints = new ArrayList(3);
            }

            _upConstraints.add(new Constraint(first, second, constraint));
        }
        else
        {
            if (_downContraints == null)
            {
                _downContraints = new ArrayList(3);
            }

            _downContraints.add(new Constraint(first, second, constraint));
        }
    }


    /**
     * Removes the constraint for the given types.
     *
     * @param first the first type.
     * @param second the second type.
     * @param constraint the type of the constraint ({@link Constraint#UP} or {@link
     *        Constraint#DOWN}).
     */
    public void removeConstraint(
        Type first,
        Type second,
        int  constraint)
    {
        if (constraint == Constraint.UP)
        {
            if (_upConstraints != null)
            {
                _upConstraints.remove(new Constraint(first, second, constraint));
            }
        }
        else
        {
            if (_downContraints != null)
            {
                _downContraints.remove(new Constraint(first, second, constraint));
            }
        }
    }


    /**
     * Updates the enabled state of the given button.
     *
     * @param button a button.
     * @param state if <code>true</code> the button will enabled.
     *
     * @since 1.0b9
     */
    private void setState(
        JButton button,
        boolean state)
    {
        if (button != null)
        {
            button.setEnabled(state);
        }
    }


    /**
     * Checks the constraints for the given types.
     *
     * @param cur current type.
     * @param prev previous type.
     *
     * @return <code>true</code> if no violation occured.
     */
    private boolean checkHighConstraint(
        Type cur,
        Type prev)
    {
        if (_upConstraints == null)
        {
            return true;
        }

        for (int i = 0, size = _upConstraints.size(); i < size; i++)
        {
            Constraint c = (Constraint) _upConstraints.get(i);

            if (c.first == cur)
            {
                if (c.second == prev)
                {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Checks the constraints for the given types.
     *
     * @param cur current type.
     * @param next previous type.
     *
     * @return <code>true</code> if no violation occured.
     */
    private boolean checkLowConstraint(
        Type cur,
        Type next)
    {
        if (_downContraints == null)
        {
            return true;
        }

        for (int i = 0, size = _downContraints.size(); i < size; i++)
        {
            Constraint c = (Constraint) _downContraints.get(i);

            if (c.first == cur)
            {
                if (c.second == next)
                {
                    return false;
                }
            }
        }

        return true;
    }


    private void initializeAddRemove()
    {
        this.addButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_ADD" /* NOI18N */));
        this.addButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    TableList.this.removeButton.setEnabled(true);
                    _tableModel.addRow(new Object[1]);
                    _table.changeSelection(_table.getRowCount() - 1, 0, false, false);
                    _table.editCellAt(_table.getRowCount() - 1, 0);

                    DefaultCellEditor editor =
                        (DefaultCellEditor) _table.getCellEditor(
                            _table.getRowCount() - 1, 0);
                    Component comp =
                        editor.getTableCellEditorComponent(
                            _table, EMPTY_STRING, true, _table.getRowCount() - 1, 0);
                    comp.requestFocus();
                }
            });

        this.removeButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_REMOVE" /* NOI18N */));
        this.removeButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // if the user is editing a value, first cancel the editing
                    TableCellEditor editor = _table.getCellEditor();

                    if (editor != null)
                    {
                        editor.cancelCellEditing();
                    }

                    int curRow = _table.getSelectionModel().getMinSelectionIndex();

                    if (curRow > -1)
                    {
                        _tableModel.removeRow(curRow);
                    }

                    if (_tableModel.getRowCount() > 0)
                    {
                        if (curRow == 0)
                        {
                            _table.changeSelection(0, 0, false, false);
                        }
                        else
                        {
                            _table.changeSelection(curRow - 1, 0, false, false);
                        }

                        TableList.this.removeButton.requestFocus();
                    }
                    else
                    {
                        TableList.this.removeButton.setEnabled(false);
                    }
                }
            });
    }


    /**
     * This method picks good column sizes.
     *
     * @param table table to initialize.
     */
    private void initializeColumnSizes(JTable table)
    {
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 1, size = table.getColumnModel().getColumnCount(); i < size; i++)
        {
            TableColumn column = table.getColumnModel().getColumn(i);
            Component comp =
                headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(), false, false, 0, 0);
            int headerWidth = comp.getPreferredSize().width;
            column.setPreferredWidth(headerWidth);
        }
    }


    /**
     * Creates a new TableList object.
     */
    private void initializeUpDown()
    {
        this.upButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_UP" /* NOI18N */));
        this.upButton.setEnabled(false);

        this.upButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    int row = _table.getSelectedRow();

                    // we know for sure that there is a row above us as we
                    // disallow the action otherwise
                    _tableModel.moveRow(row, row, row - 1);
                    _table.changeSelection(row - 1, 0, false, false);

                    if ((row - 1) != 0)
                    {
                        TableList.this.upButton.requestFocus();
                    }
                }
            });

        this.downButton =
            SwingHelper.createButton(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "BTN_DOWN" /* NOI18N */));
        this.downButton.setEnabled(false);

        this.downButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    int row = _table.getSelectedRow();

                    // we know for sure that there is a row beyond us as we
                    // disallow the action otherwise
                    _tableModel.moveRow(row, row, row + 1);
                    _table.changeSelection(row + 1, 0, false, false);

                    if ((row + 2) != _table.getRowCount())
                    {
                        TableList.this.downButton.requestFocus();
                    }
                }
            });

        // update the button states so that actions will only be allowed if
        // they make sense
        _table.getSelectionModel().addListSelectionListener(
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent ev)
                {
                    // ignore extra messages
                    if (ev.getValueIsAdjusting())
                    {
                        return;
                    }

                    updateButtons(_table.getRowCount(), _table.getSelectedRow());
                }
            });
    }


    /**
     * Updates the state of the buttons.
     *
     * @param rows the number of rows in the table.
     * @param selectedRow the currently selected row, <code>-1</code> if no row is
     *        selected.
     */
    void updateButtons(
        int rows,
        int selectedRow)
    {
        if (selectedRow == -1) // no selection
        {
            setState(this.removeButton, false);
            setState(this.upButton, false);
            setState(this.downButton, false);
        }
        else if (selectedRow == 0) // first row
        {
            setState(this.removeButton, true);
            setState(this.downButton, rows > 1);
            setState(this.upButton, false);
        }
        else if (selectedRow == (rows - 1)) // last row
        {
            setState(this.removeButton, true);
            setState(this.upButton, true);
            setState(this.downButton, false);
        }
        else // in-between row
        {
            setState(this.removeButton, true);
            setState(this.upButton, true);
            setState(this.downButton, true);
        }

        // check the constraints for the up button
        if ((this.upButton != null) && this.upButton.isEnabled())
        {
            if ((selectedRow != -1) && (_upConstraints != null))
            {
                Type prev = (Type) _tableModel.getValueAt(selectedRow - 1, 0);
                Type cur = (Type) _tableModel.getValueAt(selectedRow, 0);

                if (!checkHighConstraint(cur, prev))
                {
                    this.upButton.setEnabled(false);
                }
            }
        }

        // check the constraints for the down button
        if ((this.downButton != null) && this.downButton.isEnabled())
        {
            if ((selectedRow != -1) && (_downContraints != null))
            {
                Type cur = (Type) _tableModel.getValueAt(selectedRow, 0);
                Type next = (Type) _tableModel.getValueAt(selectedRow + 1, 0);

                if (!checkLowConstraint(cur, next))
                {
                    this.downButton.setEnabled(false);
                }
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Serves as a constraint for the type entries in a table list.
     */
    static class Constraint
    {
        /** Indicates a constraint for downwards checking. */
        public static final int DOWN = 2;

        /** Indicates a constraint for upwards checking. */
        public static final int UP = 1;

        /** The first element of the constraint. */
        Type first;

        /** The second element of the constraint. */
        Type second;

        /** The type of the constraint. */
        int constraint;

        /**
         * Creates a new Constraint object.
         * 
         * <p>
         * If you want to prohibit that element <em>first</em> can ever be moved above
         * element <em>second</em> use: <code> new Constraint(first, second,
         * Constraint.UP)</code>.
         * </p>
         * 
         * <p>
         * Accordingly, if you want to prohibit that element <em>first</em> can ever be
         * moved below element <em>second</em> use: <code> new Constraint(first, second,
         * Constraint.DOWN)</code>.
         * </p>
         *
         * @param first the first element.
         * @param second the second element.
         * @param constraint the type of the constraint; either {@link #UP} or {@link
         *        #DOWN}.
         */
        public Constraint(
            Type first,
            Type second,
            int  constraint)
        {
            this.first = first;
            this.second = second;
            this.constraint = constraint;
        }
    }


    /**
     * An editor for table cells that forces users to enter data. Once the editor gained
     * the focus, the only way to switch focus to another component is to submit some
     * data.
     *
     * @since 1.0b9
     */
    static class TableListCellEditor
        extends DefaultCellEditor
    {
        TableList tableList;

        public TableListCellEditor(
            TableList list,
            JCheckBox component)
        {
            super(component);
            initialize();

            this.tableList = list;
        }


        public TableListCellEditor(
            TableList  list,
            JTextField component)
        {
            super(component);
            initialize();

            this.tableList = list;
        }


        public TableListCellEditor(
            TableList list,
            JComboBox component)
        {
            super(component);
            initialize();

            this.tableList = list;
        }

        private void initialize()
        {
            final JComponent component = (JComponent) getComponent();

            FocusListener listener =
                new FocusAdapter()
                {
                    JDialog dialog;
                    JFrame frame;
                    GlassPane pane;
                    Component glassPane = null;

                    public void focusGained(FocusEvent ev)
                    {
                        Window window = SwingUtilities.windowForComponent(component);

                        if (window == null)
                        {
                            return;
                        }

                        if (pane == null)
                        {
                            pane =
                                new GlassPane(
                                    TableListCellEditor.this.tableList, component);
                        }

                        if (component instanceof JTextField)
                        {
                            JTextField textField = (JTextField) component;
                            String value = textField.getText();

                            pane.empty = (value.trim().length() == 0);
                        }
                        else
                        {
                            pane.empty = false;
                        }

                        if (window instanceof JFrame)
                        {
                            frame = (JFrame) window;
                            glassPane = frame.getGlassPane();
                            frame.setGlassPane(pane);
                            pane.setVisible(true);
                        }
                        else if (window instanceof JDialog)
                        {
                            dialog = (JDialog) window;
                            glassPane = dialog.getGlassPane();
                            dialog.setGlassPane(pane);
                            pane.setVisible(true);
                        }
                    }


                    public void focusLost(FocusEvent ev)
                    {
                        pane.setVisible(false);

                        if (frame != null)
                        {
                            frame.setGlassPane(glassPane);
                            frame = null;
                        }
                        else if (dialog != null)
                        {
                            dialog.setGlassPane(glassPane);
                            dialog = null;
                        }

                        glassPane = null;
                    }
                };

            component.addFocusListener(listener);
        }
    }


    /**
     * TableCellRenderer for <code>boolean</code> values.
     */
    private static class BooleanRenderer
        extends JCheckBox
        implements TableCellRenderer
    {
        boolean disabled;

        public BooleanRenderer()
        {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        public Component getTableCellRendererComponent(
            JTable  table,
            Object  value,
            boolean isSelected,
            boolean hasFocus,
            int     row,
            int     column)
        {
            if (table.isEnabled())
            {
                if (isSelected)
                {
                    setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                }
                else
                {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }

                if (!disabled && !isEnabled())
                {
                    this.setEnabled(true);
                }
            }
            else
            {
                if (isEnabled())
                {
                    this.setEnabled(false);
                    setBackground(UIManager.getColor("Table.background" /* NOI18N */));
                }
            }

            if (value == null)
            {
                this.setEnabled(false);
            }

            setSelected(((value != null) && ((Boolean) value).booleanValue()));

            return this;
        }
    }


    private static class DefaultRenderer
        extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
            JTable  table,
            Object  value,
            boolean isSelected,
            boolean hasFocus,
            int     row,
            int     column)
        {
            if (table.isEnabled())
            {
                if (isSelected)
                {
                    super.setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                }
                else
                {
                    super.setForeground(table.getForeground());
                    super.setBackground(table.getBackground());
                }
            }
            else
            {
                super.setForeground(UIManager.getColor("textInactiveText" /* NOI18N */));
                super.setBackground(UIManager.getColor("Table.background" /* NOI18N */));
            }

            setFont(table.getFont());

            if (hasFocus && table.getCellSelectionEnabled())
            {
                setBorder(
                    UIManager.getBorder("Table.focusCellHighlightBorder" /* NOI18N */));

                if (table.isCellEditable(row, column))
                {
                    super.setForeground(
                        UIManager.getColor("Table.focusCellForeground" /* NOI18N */));
                    super.setBackground(
                        UIManager.getColor("Table.focusCellBackground" /* NOI18N */));
                }
            }
            else
            {
                setBorder(noFocusBorder);
            }

            setValue(value);

            Color back = getBackground();
            boolean colorMatch =
                (back != null) && (back.equals(table.getBackground()))
                && table.isOpaque();
            setOpaque(!colorMatch);

            return this;
        }
    }


    private static class GlassPane
        extends JComponent
        implements AWTEventListener
    {
        /** The currently focused table editor component. */
        JComponent editor;
        TableList tableList;

        /**
         * Indicates whether the table editor component was empty before it gained the
         * input focus.
         */
        boolean empty;

        public GlassPane(
            TableList  tableList,
            JComponent editor)
        {
            this.tableList = tableList;
            this.editor = editor;
        }

        public void addNotify()
        {
            super.addNotify();
            Toolkit.getDefaultToolkit().addAWTEventListener(
                this,
                AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }


        public void eventDispatched(AWTEvent ev)
        {
            if (ev instanceof MouseEvent)
            {
                MouseEvent e = (MouseEvent) ev;
                e.consume();
            }
            else if (ev instanceof KeyEvent)
            {
                KeyEvent e = (KeyEvent) ev;

                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_ESCAPE :

                        if (this.empty)
                        {
                            if (this.editor instanceof JTextField)
                            {
                                JTextField textField = (JTextField) this.editor;
                                String value = textField.getText();

                                // if the user never submitted a value, we
                                // forget about the whole thing
                                if (value.trim().length() == 0)
                                {
                                    JTable table = GlassPane.this.tableList.getTable();
                                    int row =
                                        table.getSelectionModel().getMinSelectionIndex();

                                    ((DefaultTableModel) table.getModel()).removeRow(row);
                                }
                            }
                        }

                        break;
                }
            }
        }


        public void removeNotify()
        {
            try
            {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            }
            finally
            {
                super.removeNotify();
            }
        }
    }


    /**
     * A renderer for numbers. Values are right-aligned.
     */
    private static class NumberRenderer
        extends DefaultRenderer
    {
        public NumberRenderer()
        {
            super();
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }


    /**
     * TableCellRenderer for Types (actually strings).
     */
    private static class TypeRenderer
        extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
            JTable  table,
            Object  value,
            boolean isSelected,
            boolean hasFocus,
            int     row,
            int     column)
        {
            if (table.isEnabled())
            {
                if (isSelected)
                {
                    super.setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                }
                else
                {
                    super.setForeground(table.getForeground());
                    super.setBackground(table.getBackground());
                }
            }
            else
            {
                super.setForeground(UIManager.getColor("textInactiveText" /* NOI18N */));
                super.setBackground(UIManager.getColor("Table.background" /* NOI18N */));
            }

            setFont(table.getFont());
            setValue(value);

            Color back = getBackground();
            boolean colorMatch =
                (back != null) && (back.equals(table.getBackground()))
                && table.isOpaque();
            setOpaque(!colorMatch);

            return this;
        }
    }
}
