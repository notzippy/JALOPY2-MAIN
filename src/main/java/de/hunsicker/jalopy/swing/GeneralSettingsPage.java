/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.ErrorDialog;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the general Jalopy settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public class GeneralSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String JDK_1_3 = "JDK 1.3" /* NOI18N */;
    private static final String JDK_1_4 = "JDK 1.4" /* NOI18N */;
    private static final String FILENAME_IMPORT = "import.dat" /* NOI18N */;
    private static final String FILENAME_EXPORT = "export.dat" /* NOI18N */;
    private static final String JDK_1_5 = "JDK 5.0";

    //~ Instance variables ---------------------------------------------------------------

    final FileFilter FILTER_JAL = new JalopyFilter();
    final FileFilter FILTER_XML = new XmlFilter();
    JComboBox _compatComboBox;
    JTextField _descTextField;
    JTextField _nameTextField;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new GeneralSettingsPage object.
     */
    public GeneralSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new GeneralSettingsPage.
     *
     * @param container the parent container.
     */
    GeneralSettingsPage(SettingsContainer container)
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
        this.settings.put(ConventionKeys.CONVENTION_NAME, _nameTextField.getText());
        this.settings.put(
            ConventionKeys.CONVENTION_DESCRIPTION, _descTextField.getText());
        this.settings.putInt(
            ConventionKeys.SOURCE_VERSION,
            getSourceVersion((String) _compatComboBox.getSelectedItem()));
    }


    /**
     * Returns the corresponding integer constant for the given version string.
     *
     * @param version version string.
     *
     * @return corresponding integer constant.
     *
     * @since 1.0b8
     */
    private int getSourceVersion(String version)
    {
        if (JDK_1_3.equals(version))
        {
            return de.hunsicker.jalopy.language.JavaParser.JDK_1_3;
        }
        else if (JDK_1_4.equals(version))
        {
            return de.hunsicker.jalopy.language.JavaParser.JDK_1_4;
        }
        else if (JDK_1_5.equals(version))
        {
            return de.hunsicker.jalopy.language.JavaParser.JDK_1_5;
        }

        return ConventionDefaults.SOURCE_VERSION;
    }


    /**
     * Returns the corresponding string for the given integer constant.
     *
     * @param version version constant.
     *
     * @return corresponding string.
     *
     * @since 1.0b8
     */
    String getSourceVersion(int version)
    {
        switch (version)
        {
            case de.hunsicker.jalopy.language.JavaParser.JDK_1_3 :
                return JDK_1_3;

            case de.hunsicker.jalopy.language.JavaParser.JDK_1_4 :
                return JDK_1_4;
                
            case de.hunsicker.jalopy.language.JavaParser.JDK_1_5 :
            default :
                return JDK_1_5;
        }
    }


    private JPanel createGeneralPane()
    {
        JPanel conventionPanel = new JPanel();
        conventionPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_CONVENTION" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout conventionLayout = new GridBagLayout();
        conventionPanel.setLayout(conventionLayout);

        GridBagConstraints c = new GridBagConstraints();
        c.insets.right = 10;

        JLabel nameLbl = new JLabel(this.bundle.getString("LBL_NAME" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.RELATIVE, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        conventionLayout.setConstraints(nameLbl, c);
        conventionPanel.add(nameLbl, c);

        _nameTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.CONVENTION_NAME, ConventionDefaults.CONVENTION_NAME),
                15);
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        conventionLayout.setConstraints(_nameTextField, c);
        conventionPanel.add(_nameTextField);

        JLabel descLbl =
            new JLabel(this.bundle.getString("LBL_DESCRIPTION" /* NOI18N */));
        c.insets.right = 10;
        c.insets.top = 1;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.RELATIVE, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        conventionLayout.setConstraints(descLbl, c);
        conventionPanel.add(descLbl);

        _descTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.CONVENTION_DESCRIPTION,
                    ConventionDefaults.CONVENTION_DESCRIPTION), 15);
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        conventionLayout.setConstraints(_descTextField, c);
        conventionPanel.add(_descTextField);

        JPanel compatPanel = new JPanel();
        compatPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_COMPLIANCE" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout compatLayout = new GridBagLayout();
        compatPanel.setLayout(compatLayout);

        int version =
            this.settings.getInt(
                ConventionKeys.SOURCE_VERSION, ConventionDefaults.SOURCE_VERSION);
        String[] items = { JDK_1_3, JDK_1_4, JDK_1_5 };
        ComboBoxPanel compatComboBoxPanel =
            new ComboBoxPanel(
                this.bundle.getString("LBL_COMPATIBILITY" /* NOI18N */), items,
                getSourceVersion(version));
        c.insets.left = 0;
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        compatLayout.setConstraints(compatComboBoxPanel, c);
        compatPanel.add(compatComboBoxPanel, c);
        _compatComboBox = compatComboBoxPanel.getComboBox();

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(conventionPanel, c);
        panel.add(conventionPanel);

        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(compatPanel, c);
        panel.add(compatPanel);

        return panel;
    }


    private JPanel createImportExportPane()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        JButton importButton =
            SwingHelper.createButton(
                this.bundle.getString("BTN_IMPORT" /* NOI18N */), true);
        importButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    final Window owner =
                        SwingUtilities.windowForComponent(GeneralSettingsPage.this);
                    LocationDialog dialog =
                        LocationDialog.create(
                            owner,
                            GeneralSettingsPage.this.bundle.getString(
                                "TLE_IMPORT_CODE_CONVENTION" /* NOI18N */), "XXX",
                            LocationDialog.loadHistory(
                                new File(
                                    Convention.getProjectSettingsDirectory(),
                                    FILENAME_IMPORT)));

                    dialog.addFilter(FILTER_JAL);
                    dialog.addFilter(FILTER_XML, true);
                    dialog.setVisible(true);

                    switch (dialog.getOption())
                    {
                        case JOptionPane.OK_OPTION :

                            try
                            {
                                String location = (String) dialog.getSelectedLocation();

                                if ((location == null) || (location.trim().length() == 0))
                                {
                                    /**
                                     * @todo show error dialog;
                                     */
                                    return;
                                }

                                FileFilter filter = dialog.getFileFilter();

                                if (
                                    (filter == FILTER_JAL)
                                    || location.endsWith(Convention.EXTENSION_JAL))
                                {
                                    if (!location.endsWith(Convention.EXTENSION_JAL))
                                    {
                                        location += Convention.EXTENSION_JAL;
                                    }
                                }
                                else if (
                                    (filter == FILTER_XML)
                                    || location.endsWith(Convention.EXTENSION_XML))
                                {
                                    if (!location.endsWith(Convention.EXTENSION_XML))
                                    {
                                        location += Convention.EXTENSION_XML;
                                    }
                                }

                                if (location.startsWith("http:" /* NOI18N */))
                                {
                                    Convention.importSettings(
                                        new URL(location));
                                    GeneralSettingsPage.this.settings.put(
                                        ConventionKeys.STYLE_LOCATION, location);
                                }
                                else if (location.startsWith("www." /* NOI18N */))
                                {
                                    Convention.importSettings(
                                        new URL("http://" /* NOI18N */ + location));
                                    GeneralSettingsPage.this.settings.put(
                                        ConventionKeys.STYLE_LOCATION,
                                        "http://" /* NOI18N */ + location);
                                }
                                else
                                {
                                    Convention.importSettings(
                                        new File(location));
                                }

                                // update the fields
                                _nameTextField.setText(
                                    GeneralSettingsPage.this.settings.get(
                                        ConventionKeys.CONVENTION_NAME,
                                        ConventionDefaults.CONVENTION_NAME));
                                _descTextField.setText(
                                    GeneralSettingsPage.this.settings.get(
                                        ConventionKeys.CONVENTION_DESCRIPTION,
                                        ConventionDefaults.CONVENTION_DESCRIPTION));

                                int version =
                                    GeneralSettingsPage.this.settings.getInt(
                                        ConventionKeys.SOURCE_VERSION,
                                        ConventionDefaults.SOURCE_VERSION);
                                _compatComboBox.setSelectedItem(
                                    getSourceVersion(version));
                                LocationDialog.storeHistory(
                                    new File(
                                        Convention.getProjectSettingsDirectory(),
                                        FILENAME_IMPORT), dialog.getHistoryString());

                                // clear the panel cache so the new values will be
                                // loaded the next time a panel is displayed
                                if (getContainer() != null)
                                {
                                    getContainer().clearCache();
                                }

                                // and finally store everything
                                GeneralSettingsPage.this.settings.flush();
                                dialog.dispose();
                            }
                            catch (Exception ex)
                            {
                                ErrorDialog d = ErrorDialog.create(owner, ex);

                                /**
                                 * @todo needs custom error message
                                 */
                                d.setVisible(true);
                                d.dispose();
                            }

                            break;
                    }
                }
            });

        JButton exportButton =
            SwingHelper.createButton(this.bundle.getString("BTN_EXPORT" /* NOI18N */));
        exportButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    Window owner =
                        SwingUtilities.windowForComponent(GeneralSettingsPage.this);
                    LocationDialog dialog =
                        LocationDialog.create(
                            owner,
                            GeneralSettingsPage.this.bundle.getString(
                                "TLE_EXPORT_CODE_CONVENTION" /* NOI18N */), "XXX",
                            LocationDialog.loadHistory(
                                new File(
                                    Convention.getProjectSettingsDirectory(),
                                    FILENAME_EXPORT)));

                    dialog.addFilter(FILTER_XML, true);
                    dialog.setVisible(true);

                    switch (dialog.getOption())
                    {
                        case JOptionPane.OK_OPTION :

                            String location = (String) dialog.getSelectedLocation();

                            if ((location != null) && (location.length() > 0))
                            {
                                try
                                {
                                    LocationDialog.storeHistory(
                                        new File(
                                            Convention.getProjectSettingsDirectory(),
                                            FILENAME_EXPORT), dialog.getHistoryString());

                                    String extension = Convention.EXTENSION_XML;

                                    if (!location.endsWith(Convention.EXTENSION_XML))
                                    {
                                        location += Convention.EXTENSION_XML;
                                    }

                                    // export the Convention
                                    OutputStream out =
                                        new FileOutputStream(new File(location));
                                    GeneralSettingsPage.this.settings.exportSettings(
                                        out, extension);
                                }
                                catch (Exception ex)
                                {
                                    /**
                                     * @todo needs custom error message
                                     */
                                    ErrorDialog d = ErrorDialog.create(owner, ex);
                                    d.setVisible(true);
                                    d.dispose();
                                }
                            }

                            dialog.dispose();

                            break;
                    }
                }
            });
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        return buttonPanel;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 10;

        JPanel conventionPanel = createGeneralPane();
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(conventionPanel, c);
        add(conventionPanel);

        JPanel importExportPanel = createImportExportPane();
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(importExportPanel, c);
        add(importExportPanel);
    }

    //~ Inner Classes --------------------------------------------------------------------

//    TODO private static class AddDialog
//        extends JDialog
//    {
//        String value;
//
//        public AddDialog(
//            Frame  owner,
//            String title,
//            String text)
//        {
//            super(owner);
//            initialize(title, text);
//        }
//
//
//        public AddDialog(
//            Dialog owner,
//            String title,
//            String text)
//        {
//            super(owner);
//            initialize(title, text);
//        }
//
//        private void initialize(
//            String title,
//            String text)
//        {
//            setTitle(title);
//            setModal(true);
//            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//
//            Container contentPane = getContentPane();
//            GridBagLayout layout = new GridBagLayout();
//            GridBagConstraints c = new GridBagConstraints();
//            contentPane.setLayout(layout);
//
//            JLabel valueLabel = new JLabel(text);
//            c.insets.top = 10;
//            c.insets.left = 5;
//            c.insets.right = 5;
//            SwingHelper.setConstraints(
//                c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
//                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
//            layout.setConstraints(valueLabel, c);
//            contentPane.add(valueLabel);
//
//            final JTextField valueTextField = new JTextField(20);
//            valueLabel.setLabelFor(valueTextField);
//            c.insets.top = 2;
//            SwingHelper.setConstraints(
//                c, 0, 1, 12, 1, 1.0, 1.0, GridBagConstraints.WEST,
//                GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
//            layout.setConstraints(valueTextField, c);
//            contentPane.add(valueTextField);
//
//            ResourceBundle bundle =
//                ResourceBundle.getBundle("de.hunsicker.jalopy.swing.Bundle" /* NOI18N */);
//
//            final JButton cancelButton =
//                SwingHelper.createButton(bundle.getString("BTN_CANCEL" /* NOI18N */));
//            cancelButton.addActionListener(
//                new ActionListener()
//                {
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        setVisible(false);
//                        dispose();
//                    }
//                });
//
//            JButton okButton =
//                SwingHelper.createButton(bundle.getString("BTN_OK" /* NOI18N */));
//            okButton.addActionListener(
//                new ActionListener()
//                {
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        setVisible(false);
//
//                        String contents = valueTextField.getText();
//
//                        if (contents.length() == 0)
//                        {
//                            return;
//                        }
//
//                        AddDialog.this.value = contents;
//                        dispose();
//                    }
//                });
//
//            getRootPane().setDefaultButton(okButton);
//
//            c.insets.top = 15;
//            c.insets.bottom = 5;
//            SwingHelper.setConstraints(
//                c, 9, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
//                c.insets, 0, 0);
//            layout.setConstraints(okButton, c);
//            contentPane.add(okButton);
//
//            c.insets.left = 0;
//            SwingHelper.setConstraints(
//                c, 11, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
//                GridBagConstraints.WEST, GridBagConstraints.NONE, c.insets, 0, 0);
//            layout.setConstraints(cancelButton, c);
//            contentPane.add(cancelButton);
//        }
//    }


    /**
     * JFileChooser filter for Jalopy preference files (.jal).
     */
    private final class JalopyFilter
        extends FileFilter
    {
        public String getDescription()
        {
            return GeneralSettingsPage.this.bundle.getString(
                "LBL_BINARY_CONVENTION" /* NOI18N */);
        }


        public boolean accept(File f)
        {
            if (f == null)
            {
                return false;
            }

            if (f.isDirectory())
            {
                return true;
            }

            if (f.getName().endsWith(Convention.EXTENSION_JAL))
            {
                return true;
            }

            return false;
        }
    }


    /**
     * JFileChooser filter for Jalopy preference files (.xml).
     */
    private final class XmlFilter
        extends FileFilter
    {
        public String getDescription()
        {
            return GeneralSettingsPage.this.bundle.getString(
                "LBL_XML_CONVENTION" /* NOI18N */);
        }


        public boolean accept(File f)
        {
            if (f == null)
            {
                return false;
            }

            if (f.isDirectory())
            {
                return true;
            }

            if (f.getName().endsWith(Convention.EXTENSION_XML))
            {
                return true;
            }

            return false;
        }
    }
}
