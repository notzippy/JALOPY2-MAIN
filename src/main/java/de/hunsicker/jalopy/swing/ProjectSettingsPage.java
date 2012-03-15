/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.Project;
import de.hunsicker.swing.ErrorDialog;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy project settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class ProjectSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String FILENAME_PROJECT = "project.dat" /* NOI18N */;
    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    AddRemoveList _projectsList;
    private JButton _addButton;
    JButton _removeButton;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ProjectSettingsPage object.
     */
    public ProjectSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new ProjectSettingsPage.
     *
     * @param container the parent container.
     */
    ProjectSettingsPage(SettingsContainer container)
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
    }


    void setActive(ProjectListEntry entry)
    {
        DefaultListModel model = (DefaultListModel) _projectsList.getModel();

        for (int i = 0, size = model.size(); i < size; i++)
        {
            ProjectListEntry e = (ProjectListEntry) model.get(i);

            if (e.active)
            {
                e.active = false;

                break;
            }
        }

        entry.active = true;
        _projectsList.repaint();
    }


    private Project getActiveProject()
    {
        try
        {
            File file = new File(Convention.getSettingsDirectory(), FILENAME_PROJECT);

            if (file.exists())
            {
                return (Project) IoHelper.deserialize(file);
            }
            return Convention.getDefaultProject();
        }
        catch (IOException ex)
        {
            return Convention.getDefaultProject();
        }
    }


    private Collection getProjectEntries()
    {
        File directory = Convention.getSettingsDirectory();
        File[] files = directory.listFiles();
        Project activeProject = getActiveProject();
        List projects = new ArrayList(6);

        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory() && !files[i].getName().equals("default"))
            {
                File file = new File(files[i], FILENAME_PROJECT);

                try
                {
                    if (file.exists())
                    {
                        Project project = (Project) IoHelper.deserialize(file);
                        ProjectListEntry entry =
                            new ProjectListEntry(
                                project.getName(), project.getDescription(),
                                project.equals(activeProject),
                                project.equals(Convention.getDefaultProject()));
                        projects.add(entry);
                    }
                }
                catch (IOException ex)
                {
                    // should only fail between incompatible versions, just
                    // remove the file for now
                    file.delete();
                }
            }
        }

        // add the default project
        if (projects.isEmpty())
        {
            Project defaultProject = Convention.getDefaultProject();
            projects.add(
                new ProjectListEntry(
                    defaultProject.getName(), defaultProject.getDescription(), true, true));
        }
        else
        {
            boolean active = false; // is a project marked as active?

            for (int i = 0, size = projects.size(); i < size; i++)
            {
                ProjectListEntry e = (ProjectListEntry) projects.get(i);

                if (e.active)
                {
                    active = true;
                }
            }

            Project defaultProject = Convention.getDefaultProject();
            projects.add(
                new ProjectListEntry(
                    defaultProject.getName(), defaultProject.getDescription(), !active,
                    true));
        }

        Collections.sort(projects);

        return projects;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        Collection projects = getProjectEntries();
        _projectsList =
            new ProjectList(
                this.bundle.getString("TLE_ADD_PROJECT" /* NOI18N */), null, projects);
        _projectsList.setCellRenderer(new ProjectListCellRenderer());

        JScrollPane keysScrollPane = new JScrollPane(_projectsList);
        SwingHelper.setConstraints(
            c, 0, 0, 8, 8, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        layout.setConstraints(keysScrollPane, c);
        add(keysScrollPane);

        c.insets.bottom = 2;
        c.insets.top = 10;
        c.insets.left = 10;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 9, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        _addButton = _projectsList.getAddButton();
        layout.setConstraints(_addButton, c);
        add(_addButton);

        c.insets.left = 10;
        c.insets.right = 0;
        c.insets.bottom = 0;
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 9, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        _removeButton = _projectsList.getRemoveButton();
        _removeButton.setEnabled(false);
        _removeButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    ProjectListEntry entry =
                        (ProjectListEntry) _projectsList.getSelectedValue();

                    if (entry != null)
                    {
                        try
                        {
                            Convention.removeProject(
                                new Project(entry.name, entry.description));
                        }
                        catch (IOException ex)
                        {
                            JOptionPane.showMessageDialog(
                                ProjectSettingsPage.this,
                                ProjectSettingsPage.this.bundle.getString(
                                    "MSG_ERROR_REMOVING_PROJECT" /* NOI18N */),
                                ProjectSettingsPage.this.bundle.getString(
                                    "TLE_ERROR_REMOVING_PROJECT" /* NOI18N */),
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        layout.setConstraints(_removeButton, c);
        add(_removeButton);

        final JButton activateButton =
            SwingHelper.createButton(this.bundle.getString("BTN_ACTIVATE" /* NOI18N */));
        activateButton.setEnabled(false);
        c.insets.top = 15;
        SwingHelper.setConstraints(
            c, 9, 3, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(activateButton, c);
        add(activateButton);
        activateButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    ProjectListEntry entry =
                        (ProjectListEntry) _projectsList.getSelectedValue();

                    if (entry != null)
                    {
                        try
                        {
                            Convention.setProject(
                                new Project(entry.name, entry.description));
                            setActive(entry);

                            if (getContainer() != null)
                            {
                                // clear the panel cache so the new values will
                                // be populated
                                getContainer().clearCache();
                            }

                            activateButton.setEnabled(false);
                            _removeButton.setEnabled(false);
                        }
                        catch (Throwable ex)
                        {
                            ErrorDialog dialog =
                                ErrorDialog.create(
                                    SwingUtilities.windowForComponent(
                                        ProjectSettingsPage.this), ex);
                            dialog.setVisible(true);
                            dialog.dispose();
                        }
                    }
                }
            });

        _projectsList.addListSelectionListener(
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent ev)
                {
                    if (ev.getValueIsAdjusting())
                    {
                        return;
                    }

                    ProjectListEntry entry =
                        (ProjectListEntry) _projectsList.getSelectedValue();

                    if (entry != null)
                    {
                        if (entry.standard)
                        {
                            _projectsList.removeButton.setEnabled(false);
                        }
                        else
                        {
                            _projectsList.removeButton.setEnabled(true);
                        }

                        if (entry.active)
                        {
                            activateButton.setEnabled(false);
                            _projectsList.removeButton.setEnabled(false);
                        }
                        else
                        {
                            activateButton.setEnabled(true);
                        }
                    }
                    else
                    {
                        _projectsList.removeButton.setEnabled(false);
                        activateButton.setEnabled(false);
                    }
                }
            });
    }

    //~ Inner Classes --------------------------------------------------------------------

    private static final class ProjectList
        extends AddRemoveList
    {
        public ProjectList(
            String     title,
            String     text,
            Collection data)
        {
            super(title, text, data);

            EventListener[] listeners =
                this.listenerList.getListeners(ListSelectionListener.class);

            for (int i = 0; i < listeners.length; i++)
            {
                this.listenerList.remove(ListSelectionListener.class, 
                                         (ListSelectionListener)listeners[i]);
                
            }
            
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

                JLabel nameLabel =
                    new JLabel(
                        ProjectList.this.bundle.getString("LBL_NAME" /* NOI18N */));
                c.insets.top = 10;
                c.insets.left = 5;
                c.insets.right = 5;
                SwingHelper.setConstraints(
                    c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets,
                    0, 0);
                layout.setConstraints(nameLabel, c);
                contentPane.add(nameLabel);

                final JTextField nameTextField = new JTextField(20);
                nameLabel.setLabelFor(nameTextField);
                c.insets.top = 2;
                SwingHelper.setConstraints(
                    c, 0, 1, 12, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
                layout.setConstraints(nameTextField, c);
                contentPane.add(nameTextField);

                JLabel descriptionLabel =
                    new JLabel(
                        ProjectList.this.bundle.getString("LBL_DESCRIPTION" /* NOI18N */));
                c.insets.top = 10;
                c.insets.left = 5;
                c.insets.right = 5;
                SwingHelper.setConstraints(
                    c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets,
                    0, 0);
                layout.setConstraints(descriptionLabel, c);
                contentPane.add(descriptionLabel);

                final JTextArea descriptionTextArea = new JTextArea(3, 20);
                descriptionTextArea.setLineWrap(true);
                descriptionLabel.setLabelFor(descriptionTextArea);
                c.insets.top = 2;
                SwingHelper.setConstraints(
                    c, 0, 3, 12, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

                JScrollPane descriptionSrollPane = new JScrollPane(descriptionTextArea);
                layout.setConstraints(descriptionSrollPane, c);
                contentPane.add(descriptionSrollPane);

                final JButton cancelButton =
                    SwingHelper.createButton(
                        ProjectList.this.bundle.getString("BTN_CANCEL" /* NOI18N */),
                        false);
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
                        ProjectList.this.bundle.getString("BTN_OK" /* NOI18N */), false);
                okButton.addActionListener(
                    new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            String name = nameTextField.getText().trim();
                            String description = descriptionTextArea.getText().trim();
                            ProjectListEntry entry =
                                new ProjectListEntry(name, description, false);

                            if (listModel.contains(entry))
                            {
                                JOptionPane.showMessageDialog(
                                    AddDialog.this,
                                    ProjectList.this.bundle.getString(
                                        "MSG_PROJECT_EXISTS" /* NOI18N */),
                                    ProjectList.this.bundle.getString(
                                        "TLE_PROJECT_EXISTS" /* NOI18N */),
                                    JOptionPane.ERROR_MESSAGE);

                                return;
                            }
                            else if (name.trim().equals(EMPTY_STRING))
                            {
                                Object[] args = { name };
                                JOptionPane.showMessageDialog(
                                    AddDialog.this,
                                    MessageFormat.format(
                                        ProjectList.this.bundle.getString(
                                            "MSG_PROJECT_INVALID_NAME" /* NOI18N */), args),
                                    ProjectList.this.bundle.getString(
                                        "TLE_PROJECT_INVALID_NAME" /* NOI18N */),
                                    JOptionPane.ERROR_MESSAGE);

                                return;
                            }
                            else if (description.length() > 256)
                            {
                                JOptionPane.showMessageDialog(
                                    AddDialog.this,
                                    ProjectList.this.bundle.getString(
                                        "MSG_PROJECT_INVALID_DESC" /* NOI18N */),
                                    ProjectList.this.bundle.getString(
                                        "TLE_PROJECT_INVALID_DESC" /* NOI18N */),
                                    JOptionPane.ERROR_MESSAGE);

                                return;
                            }

                            try
                            {
                                Convention.addProject(new Project(name, description));

                                Object selValue = getSelectedValue();
                                listModel.add(0, entry);
                                setSelectedValue(selValue, false);
                            }
                            catch (IOException ex)
                            {
                                ErrorDialog dialog =
                                    ErrorDialog.create(AddDialog.this, ex);
                                dialog.setVisible(true);
                                dialog.dispose();
                            }
                            catch (IllegalArgumentException ex)
                            {
                                JOptionPane.showMessageDialog(
                                    AddDialog.this, ex.getMessage(),
                                    ProjectList.this.bundle.getString(
                                        "TLE_PROJECT_INVALID_NAME" /* NOI18N */),
                                    JOptionPane.ERROR_MESSAGE);

                                return;
                            }

                            dispose();
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


    private static final class ProjectListCellRenderer
        extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList   list,
            Object  value,
            int     index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            ProjectListEntry entry = (ProjectListEntry) value;
            JLabel component =
                (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            if (entry.active)
            {
                Font font = getFont().deriveFont(Font.BOLD);
                component.setFont(font);
            }

            return component;
        }
    }


    private static final class ProjectListEntry
        implements Comparable
    {
        final String description;
        final String name;
        boolean active;
        final boolean standard;

        public ProjectListEntry(
            String  name,
            String  description,
            boolean active)
        {
            this(name, description, false, false);
        }


        ProjectListEntry(
            String  name,
            String  description,
            boolean active,
            boolean standard)
        {
            this.name = name;
            this.description = description;
            this.standard = standard;
            this.active = active;
        }

        public int compareTo(Object o)
        {
            if (o instanceof ProjectListEntry)
            {
                return this.name.compareTo(((ProjectListEntry) o).name);
            }

            throw new ClassCastException();
        }


        public boolean equals(Object o)
        {
            if (o instanceof ProjectListEntry)
            {
                return this.name.equals(((ProjectListEntry) o).name);
            }

            return false;
        }


        public int hashCode()
        {
            return this.name.hashCode();
        }


        public String toString()
        {
            return this.name;
        }
    }
}
