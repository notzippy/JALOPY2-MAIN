/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import de.hunsicker.jalopy.language.DeclarationType;
import de.hunsicker.jalopy.language.ModifierType;
import de.hunsicker.jalopy.language.Type;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer sorting settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public class SortingSettingsPage
    extends AbstractSettingsPage
{
    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _sortCheckBox;
    private JCheckBox _sortModifiersCheckBox;
    private JCheckBox _sortMethodBeansCheckBox;
    private JTabbedPane _tabbedPane;

    /** The data stored in the table. */
    private List _data; // List of <List<DeclarationType>>
    private List _modifiersData; // List of <List<ModifiersType>>
    private TableList _table;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SortingSettingsPage object.
     */
    public SortingSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new SortingSettingsPage.
     *
     * @param container the parent container.
     */
    SortingSettingsPage(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getPreviewFileName()
    {
        switch (_tabbedPane.getSelectedIndex())
        {
            case 1 :
                return "sortingmodifiers" /* NOI18N */;

            default :
                return super.getPreviewFileName();
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        this.settings.putBoolean(ConventionKeys.SORT, _sortCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SORT_MODIFIERS, _sortModifiersCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SORT_METHOD_BEAN, _sortMethodBeansCheckBox.isSelected());

        String declarationOrder = getSortString();
        DeclarationType.setOrder(declarationOrder);
        this.settings.put(ConventionKeys.SORT_ORDER, declarationOrder);

        String modifierOrder = getModifierSortString();
        ModifierType.setOrder(modifierOrder);
        this.settings.put(ConventionKeys.SORT_ORDER_MODIFIERS, modifierOrder);
    }


    private String getModifierSortString()
    {
        StringBuffer buf = new StringBuffer(100);

        for (Iterator i = _modifiersData.iterator(); i.hasNext();)
        {
            List rowData = (List) i.next();
            ModifierType type = (ModifierType) rowData.get(0);
            buf.append(type.getName()+"="+((Boolean)rowData.get(1)).toString());
            buf.append(DELIMETER);
        }

        // remove the last delimeter
        buf.deleteCharAt(buf.length() - 1);

        return buf.toString();
    }


    private String getSortString()
    {
        StringBuffer buf = new StringBuffer(50);

        for (Iterator i = _data.iterator(); i.hasNext();)
        {
            List rowData = (List) i.next();
            DeclarationType type = (DeclarationType) rowData.get(0);
            buf.append(type.getName());
            buf.append(DELIMETER);

            if (type == DeclarationType.METHOD)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_METHOD, ((Boolean) rowData.get(1)).booleanValue());
            }
            else if (type == DeclarationType.CTOR)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_CTOR, ((Boolean) rowData.get(1)).booleanValue());
            }
            else if (type == DeclarationType.CLASS)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_CLASS, ((Boolean) rowData.get(1)).booleanValue());
            }
            else if (type == DeclarationType.VARIABLE)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_VARIABLE,
                    ((Boolean) rowData.get(1)).booleanValue());
            }
            else if (type == DeclarationType.INTERFACE)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_INTERFACE,
                    ((Boolean) rowData.get(1)).booleanValue());
            }
            else if (type == DeclarationType.ENUM)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_ENUM,
                    ((Boolean) rowData.get(1)).booleanValue());
            }
            else if (type == DeclarationType.ANNOTATION)
            {
                this.settings.putBoolean(
                    ConventionKeys.SORT_ANNOTATION,
                    ((Boolean) rowData.get(1)).booleanValue());
            }
            else {
                //throw new IllegalArgumentException("Unknown type " +type);
            }
        }

        // remove the last delimeter
        buf.deleteCharAt(buf.length() - 1);

        return buf.toString();
    }


    private JPanel createDeclarationPane()
    {
        StringTokenizer tokens =
            new StringTokenizer(
                this.settings.get(ConventionKeys.SORT_ORDER, DeclarationType.getOrder()),
                DELIMETER);
        Object[][] data = new Object[9][2];

        for (int i = 0; tokens.hasMoreTokens(); i++)
        {
            String token = tokens.nextToken();

            if (DeclarationType.valueOf(token) == DeclarationType.VARIABLE)
            {
                data[i][0] = DeclarationType.VARIABLE;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_VARIABLE, ConventionDefaults.SORT_VARIABLE));
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.INIT)
            {
                data[i][0] = DeclarationType.INIT;
                data[i][1] = null;
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.CTOR)
            {
                data[i][0] = DeclarationType.CTOR;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_CTOR, ConventionDefaults.SORT_CTOR));
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.METHOD)
            {
                data[i][0] = DeclarationType.METHOD;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_METHOD, ConventionDefaults.SORT_METHOD));
            }
            else if (
                DeclarationType.valueOf(token) == DeclarationType.STATIC_VARIABLE_INIT)
            {
                data[i][0] = DeclarationType.STATIC_VARIABLE_INIT;
                data[i][1] = null;
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.INTERFACE)
            {
                data[i][0] = DeclarationType.INTERFACE;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_INTERFACE,
                            ConventionDefaults.SORT_INTERFACE));
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.CLASS)
            {
                data[i][0] = DeclarationType.CLASS;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_CLASS, ConventionDefaults.SORT_CLASS));
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.ANNOTATION)
            {
                data[i][0] = DeclarationType.ANNOTATION;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_ANNOTATION, ConventionDefaults.SORT_ANNOTATION));
            }
            else if (DeclarationType.valueOf(token) == DeclarationType.ENUM)
            {
                data[i][0] = DeclarationType.ENUM;
                data[i][1] =
                    new Boolean(
                        this.settings.getBoolean(
                            ConventionKeys.SORT_ENUM, ConventionDefaults.SORT_ENUM));
            }
            else {
                System.out.println("Unknown !");
            }
        }

        JPanel general = new JPanel();
        general.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        general.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _sortCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_SORT_CLASS_ELEMENTS" /* NOI18N */),
                this.settings.getBoolean(ConventionKeys.SORT, ConventionDefaults.SORT));
        _sortCheckBox.addActionListener(this.trigger);
        general.add(_sortCheckBox);
        _sortCheckBox.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    refresh();
                }
            });

        JPanel typesPanel = new JPanel();
        typesPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_ORDERING" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 0)));

        GridBagLayout typesLayout = new GridBagLayout();
        typesPanel.setLayout(typesLayout);

        Object[] columnNames =
        {
            this.bundle.getString("HDR_TYPE" /* NOI18N */),
            this.bundle.getString("HDR_SORT" /* NOI18N */)
        };

        DefaultTableModel d = new DataModel(data, columnNames);
        _table = new TableList(d, TableList.TYPE_UP_DOWN);
        _table.addConstraint(
            DeclarationType.INIT, DeclarationType.VARIABLE, TableList.Constraint.UP);
        _table.addConstraint(
            DeclarationType.VARIABLE, DeclarationType.INIT, TableList.Constraint.DOWN);
        _data = d.getDataVector();

        GridBagConstraints c = new GridBagConstraints();
        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        typesLayout.setConstraints(_table, c);
        typesPanel.add(_table);

        c.insets.top = 10;
        c.insets.bottom = 2;
        c.insets.left = 10;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 9, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        typesLayout.setConstraints(_table.getUpButton(), c);
        typesPanel.add(_table.getUpButton());

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 2, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        typesLayout.setConstraints(_table.getDownButton(), c);
        typesPanel.add(_table.getDownButton());

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(general, c);
        panel.add(general);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(typesPanel, c);
        panel.add(typesPanel);

        refresh();

        return panel;
    }


    private JPanel createModifierPane()
    {
        StringTokenizer tokens =
            new StringTokenizer(
                this.settings.get(
                    ConventionKeys.SORT_ORDER_MODIFIERS, ModifierType.getOrder()),
                DELIMETER);
        Object[][] data = new Object[11][2];

        for (int i = 0; tokens.hasMoreTokens(); i++)
        {
            String token = tokens.nextToken();

            if (ModifierType.valueOf(token) == ModifierType.PUBLIC)
            {
                data[i][0] = ModifierType.PUBLIC;
                data[i][1] = Boolean.valueOf(ModifierType.PUBLIC.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.PROTECTED)
            {
                data[i][0] = ModifierType.PROTECTED;
                data[i][1] = Boolean.valueOf(ModifierType.PROTECTED.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.PRIVATE)
            {
                data[i][0] = ModifierType.PRIVATE;
                data[i][1] = Boolean.valueOf(ModifierType.PRIVATE.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.STATIC)
            {
                data[i][0] = ModifierType.STATIC;
                data[i][1] = Boolean.valueOf(ModifierType.STATIC.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.FINAL)
            {
                data[i][0] = ModifierType.FINAL;
                data[i][1] = Boolean.valueOf(ModifierType.FINAL.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.ABSTRACT)
            {
                data[i][0] = ModifierType.ABSTRACT;
                data[i][1] = Boolean.valueOf(ModifierType.ABSTRACT.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.NATIVE)
            {
                data[i][0] = ModifierType.NATIVE;
                data[i][1] = Boolean.valueOf(ModifierType.NATIVE.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.TRANSIENT)
            {
                data[i][0] = ModifierType.TRANSIENT;
                data[i][1] = Boolean.valueOf(ModifierType.TRANSIENT.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.SYNCHRONIZED)
            {
                data[i][0] = ModifierType.SYNCHRONIZED;
                data[i][1] = Boolean.valueOf(ModifierType.SYNCHRONIZED.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.VOLATILE)
            {
                data[i][0] = ModifierType.VOLATILE;
                data[i][1] = Boolean.valueOf(ModifierType.VOLATILE.getSort());
            }
            else if (ModifierType.valueOf(token) == ModifierType.STRICTFP)
            {
                data[i][0] = ModifierType.STRICTFP;
                data[i][1] = Boolean.valueOf(ModifierType.STRICTFP.getSort());
            }
        }

        JPanel general = new JPanel();
        general.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        general.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        
        _sortModifiersCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_SORT_MODIFIERS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SORT_MODIFIERS, ConventionDefaults.SORT_MODIFIERS));
        _sortModifiersCheckBox.addActionListener(this.trigger);
        general.add(_sortModifiersCheckBox);

        _sortModifiersCheckBox.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    refresh();
                }
            });
_sortMethodBeansCheckBox = 
                new JCheckBox(
                "Sort method names as beans",
                this.settings.getBoolean(
                    ConventionKeys.SORT_METHOD_BEAN, ConventionDefaults.SORT_METHOD_BEAN));
        _sortMethodBeansCheckBox.addActionListener(this.trigger);
        general.add(_sortMethodBeansCheckBox);

        _sortMethodBeansCheckBox.addActionListener(this.trigger);

        JPanel typesPanel = new JPanel();
        typesPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_ORDERING" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 0)));

        GridBagLayout typesLayout = new GridBagLayout();
        typesPanel.setLayout(typesLayout);

        Object[] columnNames = {
            this.bundle.getString("HDR_TYPE" /* NOI18N */),
            this.bundle.getString("HDR_SORT" /* NOI18N */)
                                };
        DefaultTableModel d = new DataModel(data, columnNames);
        TableList modifiersTable = new TableList(d, TableList.TYPE_UP_DOWN);
        // TODO JTable table = modifiersTable.getTable();
        d.addTableModelListener(
            new TableModelListener()
            {
                public void tableChanged(TableModelEvent ev)
                {
                    trigger.actionPerformed(null);
                }
            });
        _modifiersData = d.getDataVector();

        GridBagConstraints c = new GridBagConstraints();

        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        typesLayout.setConstraints(modifiersTable, c);
        typesPanel.add(modifiersTable);

        c.insets.top = 10;
        c.insets.bottom = 2;
        c.insets.left = 10;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 9, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

        JButton upButton = modifiersTable.getUpButton();
        typesLayout.setConstraints(upButton, c);
        typesPanel.add(upButton);
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 2, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

        JButton downButton = modifiersTable.getDownButton();
        typesLayout.setConstraints(downButton, c);
        typesPanel.add(downButton);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        c.insets.left = 5;
        c.insets.right = 5;
        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(general, c);
        panel.add(general);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(typesPanel, c);
        panel.add(typesPanel);

        return panel;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        _tabbedPane = new JTabbedPane();
        _tabbedPane.add(
            createDeclarationPane(),
            this.bundle.getString("TAB_DECLARATIONS" /* NOI18N */));
        _tabbedPane.add(
            createModifierPane(), this.bundle.getString("TAB_MODIFIERS" /* NOI18N */));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(_tabbedPane, BorderLayout.CENTER);

        if (getContainer() != null)
        {
            _tabbedPane.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent ev)
                    {
                        String text = getContainer().loadPreview(getPreviewFileName());
                        getContainer().getPreview().setText(text);
                    }
                });
        }
    }


    /**
     * Sets the state (enabled/disabled) of the panels according to the selection state
     * of the sorting checkbox.
     */
    void refresh()
    {
        if (_sortCheckBox.isSelected())
        {
            _table.setEnabled(true);
        }
        else
        {
            _table.setEnabled(false);
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Provides the data do be displayed in the table.
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

        public boolean isCellEditable(
            int row,
            int col)
        {
            if (getValueAt(row, col) == null)
            {
                return false;
            }

            if (col < 1)
            {
                return false;
            }
            return true;
        }


        public Class getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case 0 :
                    return Type.class;

                case 1 :
                    return Boolean.class;

                default :
                    return String.class;
            }
        }
    }
}
