/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Environment;
import de.hunsicker.swing.util.SwingHelper;

/**
 * Settings page for the Jalopy printer environment settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class EnvironmentSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Used to track user actions (additions/removals). */
    static Map _changes = new HashMap(); // Map of <ListEntry>:<Object>

    /** Indicates an addition to the list. */
    static final Object ACTION_ADD = new Object();

    /** Indicates a removal from the list. */
    static final Object ACTION_REMOVE = new Object();
    private static final char DELIM_PAIR = '^';
    private static final String EMPTY_STRING = "".intern(); /* NOI18N */

    /** The pattern to validate variables . */
//     static Pattern _variablesPattern;

    /** The pattern matcher. */
    static final Matcher  _matcher = Pattern.compile(
    "[a-zA-Z_][a-zA-Z0-9_]*").matcher("a");
/*
    static
    {
		

			Pattern  compiler = Pattern.compile(
                    "[a-zA-Z_][a-zA-Z0-9_]*");
    }
	*/

    AddRemoveList _variablesList;
    private JButton _addButton;
    private JButton _removeButton;
    private JTabbedPane _tabs;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new EnvironmentSettingsPage object.
     */
    public EnvironmentSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new EnvironmentSettingsPage.
     *
     * @param container the parent container.
     */
    EnvironmentSettingsPage(SettingsContainer container)
    {
        super(container);
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        DefaultListModel keysListModel = (DefaultListModel) _variablesList.getModel();

        if (keysListModel.size() > 0)
        {
            ListEntry[] items = new ListEntry[keysListModel.size()];
            keysListModel.copyInto(items);

            StringBuffer buf = new StringBuffer(100);

            for (int i = 0; i < items.length; i++)
            {
                buf.append(items[i].variable);
                buf.append(DELIM_PAIR);
                buf.append(items[i].value);
                buf.append(DELIMETER);
            }

            buf.deleteCharAt(buf.length() - 1);
            this.settings.put(ConventionKeys.ENVIRONMENT, buf.toString());
        }
        else
        {
            this.settings.put(ConventionKeys.ENVIRONMENT, EMPTY_STRING);
        }

        Environment env = Environment.getInstance();

        // update the environment so the changes will be available
        // immediately (important only for the IDE Plug-ins)
        for (Iterator i = _changes.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            ListEntry e = (ListEntry) entry.getKey();
            Object action = entry.getValue();

            if (action == ACTION_REMOVE)
            {
                env.unset(e.variable);
            }
            else
            {
                env.set(e.variable, e.value);
            }
        }

        _changes.clear();
    }


    /**
     * Creates the pane with the system environment variables.
     *
     * @return pane with system environment variables.
     *
     * @since 1.0b8
     */
    private JPanel createSystemPane()
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        List variables = new ArrayList();

        for (Iterator i = System.getProperties().entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            variables.add(
                new ListEntry((String) entry.getKey(), (String) entry.getValue()));
        }

        Collections.sort(variables);

        EnvironmentList envList = new EnvironmentList(EMPTY_STRING, null, variables);
        JScrollPane envListScrollPane = new JScrollPane(envList);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        layout.setConstraints(envListScrollPane, c);
        panel.add(envListScrollPane);

        return panel;
    }


    /**
     * Creates the pane with the user environment variables.
     *
     * @return pane with user environment variables.
     *
     * @since 1.0b8
     */
    private JPanel createUserPane()
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        String variablesString =
            this.settings.get(ConventionKeys.ENVIRONMENT, ConventionDefaults.ENVIRONMENT);
        List variables = Collections.EMPTY_LIST;

        if ((variablesString != null) && (!variablesString.trim().equals(EMPTY_STRING)))
        {
            variables = new ArrayList();

            for (
                StringTokenizer tokens = new StringTokenizer(variablesString, DELIMETER);
                tokens.hasMoreElements();)
            {
                String v = tokens.nextToken();
                int offset = v.indexOf(DELIM_PAIR);
                String variable = v.substring(0, offset);
                String value = v.substring(offset + 1);
                variables.add(new ListEntry(variable, value));
            }
        }

        _variablesList =
            new EnvironmentList(
                this.bundle.getString("TLE_ADD_NEW_VARIABLE" /* NOI18N */), null,
                variables);

        JScrollPane keysScrollPane = new JScrollPane(_variablesList);
        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        layout.setConstraints(keysScrollPane, c);
        panel.add(keysScrollPane);

        c.insets.bottom = 2;
        c.insets.top = 10;
        c.insets.left = 10;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 9, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        _addButton = _variablesList.getAddButton();
        layout.setConstraints(_addButton, c);
        panel.add(_addButton);

        c.insets.left = 10;
        c.insets.right = 0;
        c.insets.bottom = 0;
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 2, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        _removeButton = _variablesList.getRemoveButton();
        _removeButton.setEnabled(false);
        layout.setConstraints(_removeButton, c);
        _removeButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    _changes.put(_variablesList.getSelectedValue(), ACTION_REMOVE);
                }
            });
        panel.add(_removeButton);

        return panel;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        _tabs = new JTabbedPane();

        JPanel userPanel = createUserPane();
        _tabs.add(userPanel, this.bundle.getString("TAB_USER" /* NOI18N */));

        JPanel systemPane = createSystemPane();
        _tabs.add(systemPane, this.bundle.getString("TAB_SYSTEM" /* NOI18N */));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(_tabs, BorderLayout.CENTER);
    }

    //~ Inner Classes --------------------------------------------------------------------

    private class EnvironmentList
        extends AddRemoveList
    {
        public EnvironmentList(
            String     title,
            String     text,
            Collection data)
        {
            super(title, text, data);
        }

        protected JDialog getAddDialog(Frame owner)
        {
            return new AddDialog(owner, this.title, this.text);
        }


        protected JDialog getAddDialog(Dialog owner)
        {
            return new AddDialog(owner, this.title, this.text);
        }

        private class AddDialog
            extends JDialog
        {
            public AddDialog(
                Frame  owner,
                String title,
                String text)
            {
                super(owner);
                initialize(title, text);
            }


            public AddDialog(
                Dialog owner,
                String title,
                String text)
            {
                super(owner);
                initialize(title, text);
            }

            private void initialize(
                String newTitle,
                String newText)
            {
                setTitle(newTitle);
                setModal(true);
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                Container contentPane = getContentPane();
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints c = new GridBagConstraints();
                contentPane.setLayout(layout);

                JLabel variableLabel =
                    new JLabel(
                        EnvironmentSettingsPage.this.bundle.getString(
                            "LBL_VARIABLE" /* NOI18N */));
                c.insets.top = 10;
                c.insets.left = 5;
                c.insets.right = 5;
                SwingHelper.setConstraints(
                    c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets,
                    0, 0);
                layout.setConstraints(variableLabel, c);
                contentPane.add(variableLabel);

                final JTextField variableTextField = new JTextField(20);
                variableLabel.setLabelFor(variableTextField);
                c.insets.top = 2;
                SwingHelper.setConstraints(
                    c, 0, 1, 12, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
                layout.setConstraints(variableTextField, c);
                contentPane.add(variableTextField);

                JLabel valueLabel =
                    new JLabel(
                        EnvironmentSettingsPage.this.bundle.getString(
                            "LBL_VALUE" /* NOI18N */));
                c.insets.top = 10;
                c.insets.left = 5;
                c.insets.right = 5;
                SwingHelper.setConstraints(
                    c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets,
                    0, 0);
                layout.setConstraints(valueLabel, c);
                contentPane.add(valueLabel);

                final JTextField valueTextField = new JTextField(20);
                valueLabel.setLabelFor(valueTextField);
                c.insets.top = 2;
                SwingHelper.setConstraints(
                    c, 0, 3, 12, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
                layout.setConstraints(valueTextField, c);
                contentPane.add(valueTextField);

                final JButton cancelButton =
                    SwingHelper.createButton(
                        EnvironmentSettingsPage.this.bundle.getString(
                            "BTN_CANCEL" /* NOI18N */), false);
                cancelButton.addActionListener(
                    new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            setVisible(false);
                            dispose();
                        }
                    });

                JButton okButton =
                    SwingHelper.createButton(
                        EnvironmentSettingsPage.this.bundle.getString(
                            "BTN_OK" /* NOI18N */), false);
                okButton.addActionListener(
                    new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            String variable = variableTextField.getText().trim();
                            String value = valueTextField.getText().trim();
                            ListEntry entry = new ListEntry(variable, value);

                            if (listModel.contains(entry))
                            {
                                /**
                                 * @todo show dialog
                                 */
                                return;
                            }

                            if (!_matcher.reset(variable).matches())
                            {
                                Object[] args =
                                { variable, _matcher.pattern() };
                                JOptionPane.showMessageDialog(
                                    AddDialog.this,
                                    MessageFormat.format(
                                        EnvironmentSettingsPage.this.bundle.getString(
                                            "MSG_INVALID_VARIABLE" /* NOI18N */), args),
                                    EnvironmentSettingsPage.this.bundle.getString(
                                        "TLE_INVALID_VARIABLE" /* NOI18N */),
                                    JOptionPane.ERROR_MESSAGE);

                                return;
                            }

                            listModel.add(0, entry);
                            setSelectedIndex(0);
                            dispose();
                            _changes.put(entry, ACTION_ADD);
                        }
                    });
                getRootPane().setDefaultButton(okButton);

                c.insets.top = 15;
                c.insets.bottom = 5;
                SwingHelper.setConstraints(
                    c, 9, 4, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
                    GridBagConstraints.NONE, c.insets, 0, 0);
                layout.setConstraints(okButton, c);
                contentPane.add(okButton);

                c.insets.left = 0;
                SwingHelper.setConstraints(
                    c, 11, 4, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, c.insets, 0, 0);
                layout.setConstraints(cancelButton, c);
                contentPane.add(cancelButton);
            }
        }
    }


    /**
     * Represents an entry in the list: a key/value pair.
     */
    private static class ListEntry
        implements Comparable
    {
        public String value;
        public String variable;

        public ListEntry(
            String variable,
            String value)
        {
            this.variable = variable;
            this.value = value;
        }

        public int compareTo(Object o)
        {
            if (o instanceof ListEntry)
            {
                return this.variable.compareTo(((ListEntry) o).variable);
            }

            return 0;
        }


        public boolean equals(Object o)
        {
            if (o instanceof ListEntry)
            {
                return this.variable.equals(((ListEntry) o).variable);
            }

            return false;
        }


        public int hashCode()
        {
            return this.variable.hashCode();
        }


        public String toString()
        {
            StringBuffer buf = new StringBuffer(30);
            buf.append(this.variable);
            buf.append(' ');
            buf.append('=');
            buf.append(' ');
            buf.append(this.value);

            return buf.toString();
        }
    }
}
