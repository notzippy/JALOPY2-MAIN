/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.History;
import de.hunsicker.swing.EmptyButtonGroup;
import de.hunsicker.swing.ErrorDialog;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the miscellaneous Jalopy settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
public class MiscSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    static final String EMPTY_STRING = "" /* NOI18N */.intern();
    private static final String FILENAME_BACKUP = "backup.dat" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _arrayBracketsAfterIdentifierCheckBox;
    private JCheckBox _forceCheckBox;
    private JCheckBox _insertFinalModifierCheckBox;
    private JCheckBox _insertMethodFinalModifierCheckBox;
    private JCheckBox _historyCommentCheckBox;
    JCheckBox _historyFileCheckBox;
    private JCheckBox _insertConditionalCheckBox;
    private JCheckBox _insertParenCheckBox;
    private JCheckBox _insertTrailingNewlineCheckBox;
    private JCheckBox _insertUIDCheckBox;
    JComboBox _historyMethodComboBox;
    private JSlider _backupSlider;
    private JSlider _threadSlider;
    JTextField _directoryTextField;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new MiscSettingsPage object.
     */
    public MiscSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new MiscSettingsPage.
     *
     * @param container the parent container.
     */
    MiscSettingsPage(SettingsContainer container)
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
        this.settings.putBoolean(
            ConventionKeys.INSERT_EXPRESSION_PARENTHESIS,
            _insertParenCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.FORCE_FORMATTING, _forceCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INSERT_FINAL_MODIFIER_FOR_PARAMETERS, _insertFinalModifierCheckBox.isSelected());
        
        this.settings.putBoolean(
            ConventionKeys.INSERT_FINAL_MODIFIER_FOR_METHOD_PARAMETERS, _insertMethodFinalModifierCheckBox.isSelected());        
        this.settings.putBoolean(
            ConventionKeys.INSERT_TRAILING_NEWLINE,
            _insertTrailingNewlineCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ARRAY_BRACKETS_AFTER_IDENT,
            _arrayBracketsAfterIdentifierCheckBox.isSelected());
        this.settings.putInt(ConventionKeys.BACKUP_LEVEL, _backupSlider.getValue());
        this.settings.putInt(ConventionKeys.THREAD_COUNT, _threadSlider.getValue());

        String directoryPath = _directoryTextField.getText();

        if (
            directoryPath.startsWith(
                Convention.getProjectSettingsDirectory().getAbsolutePath()))
        {
            // if the user specified a path relative to the default Jalopy
            // settings directory we only store the relative subdirectory
            // to make the setting portable across different platforms
            directoryPath =
                directoryPath.substring(
                    Convention.getProjectSettingsDirectory().getAbsolutePath().length()
                    + 1);
        }

        this.settings.put(ConventionKeys.BACKUP_DIRECTORY, directoryPath);
        this.settings.putBoolean(
            ConventionKeys.INSERT_SERIAL_UID, _insertUIDCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INSERT_LOGGING_CONDITIONAL,
            _insertConditionalCheckBox.isSelected());

        if (_historyCommentCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.HISTORY_POLICY, History.Policy.COMMENT.getName());
        }
        else if (_historyFileCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.HISTORY_POLICY, History.Policy.FILE.getName());

            History.Method historyMethod =
                (History.Method) _historyMethodComboBox.getSelectedItem();
            this.settings.put(ConventionKeys.HISTORY_METHOD, historyMethod.getName());
        }
        else
        {
            this.settings.put(
                ConventionKeys.HISTORY_POLICY, History.Policy.DISABLED.getName());
        }
    }


    private JPanel createBackupPanel()
    {
        JPanel backupPanel = new JPanel();
        backupPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_BACKUP" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout backupLayout = new GridBagLayout();
        backupPanel.setLayout(backupLayout);

        GridBagConstraints c = new GridBagConstraints();

        JLabel backupLbl =
            new JLabel(this.bundle.getString("LBL_BACKUP_LEVEL" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        backupLayout.setConstraints(backupLbl, c);
        backupPanel.add(backupLbl);

        _backupSlider =
            new JSlider(
                SwingConstants.HORIZONTAL, 0, 30,
                this.settings.getInt(
                    ConventionKeys.BACKUP_LEVEL, ConventionDefaults.BACKUP_LEVEL));
        _backupSlider.setSnapToTicks(true);
        c.insets.left = 10;
        c.insets.right = 10;
        SwingHelper.setConstraints(
            c, 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        backupLayout.setConstraints(_backupSlider, c);
        backupPanel.add(_backupSlider);

        final NumberedLabel backupLevelLabel =
            new NumberedLabel(
                this.settings.getInt(
                    ConventionKeys.BACKUP_LEVEL, ConventionDefaults.BACKUP_LEVEL),
                this.bundle.getString("LBL_BACKUP_SINGULAR" /* NOI18N */),
                this.bundle.getString("LBL_BACKUP_PLURAL" /* NOI18N */));
        _backupSlider.addChangeListener(
            new ChangeListener()
            {
                public void stateChanged(ChangeEvent ev)
                {
                    JSlider source = (JSlider) ev.getSource();
                    int level = source.getValue();
                    backupLevelLabel.setLevel(level);
                }
            });
        c.insets.left = 0;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 2, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        backupLayout.setConstraints(backupLevelLabel, c);
        backupPanel.add(backupLevelLabel);
        c.insets.top = 10;

        JLabel directoryLbl =
            new JLabel(this.bundle.getString("LBL_BACKUP_DIRECTORY" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        backupLayout.setConstraints(directoryLbl, c);
        backupPanel.add(directoryLbl);

        String directoryPath =
            this.settings.get(
                ConventionKeys.BACKUP_DIRECTORY,
                Convention.getBackupDirectory().getAbsolutePath());
        File directoryFile = new File(directoryPath);

        if (!directoryFile.isAbsolute())
        {
            directoryPath =
                new File(Convention.getProjectSettingsDirectory(), directoryPath)
                .getAbsolutePath();
        }

        _directoryTextField = new JTextField(directoryPath);
        _directoryTextField.setCaretPosition(1);
        _directoryTextField.setEditable(false);
        _directoryTextField.setToolTipText(_directoryTextField.getText());
        c.insets.left = 10;
        c.insets.right = 10;
        SwingHelper.setConstraints(
            c, 1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        backupLayout.setConstraints(_directoryTextField, c);
        backupPanel.add(_directoryTextField);

        final JButton chooseButton =
            SwingHelper.createButton(this.bundle.getString("BTN_CHOOSE" /* NOI18N */));
        c.insets.left = 0;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 2, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        backupLayout.setConstraints(chooseButton, c);
        backupPanel.add(chooseButton);
        chooseButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    final Window owner =
                        SwingUtilities.windowForComponent(MiscSettingsPage.this);

                    String backupHistory =
                        LocationDialog.loadHistory(
                            new File(
                                Convention.getProjectSettingsDirectory(), FILENAME_BACKUP));

                    if (EMPTY_STRING.equals(backupHistory))
                    {
                        backupHistory = Convention.getBackupDirectory().getAbsolutePath();
                    }

                    LocationDialog dialog =
                        LocationDialog.create(
                            owner,
                            MiscSettingsPage.this.bundle.getString(
                                "TLE_SELECT_BACKUP_DIRECTORY" /* NOI18N */), "XXX",
                            backupHistory);

                    dialog.setFileChooserProperties(LocationDialog.DIRECTORIES_ONLY);
                    dialog.setVisible(true);

                    switch (dialog.getOption())
                    {
                        case JOptionPane.OK_OPTION :

                            try
                            {
                                String location = (String) dialog.getSelectedLocation();

                                if ((location != null) && (location.length() > 0))
                                {
                                    File file = new File(location);

                                    if (!IoHelper.ensureDirectoryExists(file))
                                    {
                                        /**
                                         * @todo display message
                                         */
                                        return;
                                    }

                                    _directoryTextField.setText(file.getAbsolutePath());
                                    _directoryTextField.setToolTipText(
                                        file.getAbsolutePath());

                                    // update url history
                                    LocationDialog.storeHistory(
                                        new File(
                                            Convention.getProjectSettingsDirectory(),
                                            FILENAME_BACKUP), dialog.getHistoryString());
                                }

                                dialog.dispose();
                            }
                            catch (Throwable ex)
                            {
                                /**
                                 * @todo needs custom error message
                                 */
                                ErrorDialog d = ErrorDialog.create(owner, ex);
                                d.setVisible(true);
                                d.dispose();
                            }

                            break;
                    }
                }
            });

        return backupPanel;
    }


    private JPanel createHistoryPanel()
    {
        JPanel historyPanel = new JPanel();
        historyPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_HISTORY" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        History.Policy historyPolicy =
            History.Policy.valueOf(
                this.settings.get(
                    ConventionKeys.HISTORY_POLICY, ConventionDefaults.HISTORY_POLICY));
        GridBagLayout historyLayout = new GridBagLayout();
        historyPanel.setLayout(historyLayout);

        GridBagConstraints c = new GridBagConstraints();

        _historyFileCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_USE_HISTORY_FILE" /* NOI18N */),
                historyPolicy == History.Policy.FILE);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        historyLayout.setConstraints(_historyFileCheckBox, c);
        historyPanel.add(_historyFileCheckBox);

        History.Method historyMethod =
            History.Method.valueOf(
                this.settings.get(
                    ConventionKeys.HISTORY_METHOD, ConventionDefaults.HISTORY_METHOD));
        ComboBoxPanel historyMethodCombo =
            new ComboBoxPanel(
                "method" /* NOI18N */,
                new Object[]
                {
                    History.Method.TIMESTAMP, History.Method.CRC32, History.Method.ADLER32
                }, historyMethod);
        _historyMethodComboBox = historyMethodCombo.getComboBox();
        _historyMethodComboBox.setEnabled(_historyFileCheckBox.isSelected());
        SwingHelper.setConstraints(
            c, 1, 0, 1, 1, 0.5, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        historyLayout.setConstraints(_historyMethodComboBox, c);
        historyPanel.add(_historyMethodComboBox);

        final JButton browseButton =
            SwingHelper.createButton(this.bundle.getString("BTN_DLG_VIEW" /* NOI18N */));
        browseButton.setEnabled(_historyFileCheckBox.isSelected());

        _historyFileCheckBox.addChangeListener(
            new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    if (_historyFileCheckBox.isSelected())
                    {
                        browseButton.setEnabled(true);
                        _historyMethodComboBox.setEnabled(true);
                    }
                    else
                    {
                        browseButton.setEnabled(false);
                        _historyMethodComboBox.setEnabled(false);
                    }
                }
            });

        browseButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JDialog dialog = null;
                    Window owner =
                        SwingUtilities.windowForComponent(MiscSettingsPage.this);

                    if (owner instanceof Dialog)
                    {
                        dialog =
                            new JDialog(
                                (Dialog) owner,
                                MiscSettingsPage.this.bundle.getString(
                                    "TLE_HISTORY_VIEWER" /* NOI18N */));
                    }
                    else
                    {
                        dialog =
                            new JDialog(
                                (Frame) owner,
                                MiscSettingsPage.this.bundle.getString(
                                    "TLE_HISTORY_VIEWER" /* NOI18N */));
                    }

                    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    dialog.getContentPane().add(new HistoryViewer());
                    dialog.setModal(true);
                    dialog.pack();
                    dialog.setLocationRelativeTo(owner);
                    dialog.setVisible(true);
                    dialog.dispose();
                }
            });
        SwingHelper.setConstraints(
            c, 2, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        historyLayout.setConstraints(browseButton, c);
        historyPanel.add(browseButton);

        _historyCommentCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_USE_HISTORY_COMMENT" /* NOI18N */),
                historyPolicy == History.Policy.COMMENT);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        historyLayout.setConstraints(_historyCommentCheckBox, c);
        historyPanel.add(_historyCommentCheckBox);

        ButtonGroup historyCheckBoxGroup = new EmptyButtonGroup();
        historyCheckBoxGroup.add(_historyCommentCheckBox);
        historyCheckBoxGroup.add(_historyFileCheckBox);

        return historyPanel;
    }


    private JPanel createThreadPanel()
    {
        JPanel threadPanel = new JPanel();
        threadPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_THREADS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout threadPanelLayout = new GridBagLayout();
        threadPanel.setLayout(threadPanelLayout);

        GridBagConstraints c = new GridBagConstraints();
        JLabel threadLabel =
            new JLabel(this.bundle.getString("LBL_THREADS_NUMBER" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        threadPanelLayout.setConstraints(threadLabel, c);
        threadPanel.add(threadLabel);

        _threadSlider =
            new JSlider(
                SwingConstants.HORIZONTAL, 1, 8,
                this.settings.getInt(
                    ConventionKeys.THREAD_COUNT, ConventionDefaults.THREAD_COUNT));
        _threadSlider.setLabelTable(_threadSlider.createStandardLabels(1, 1));
        _threadSlider.setMajorTickSpacing(7);
        _threadSlider.setMinorTickSpacing(1);
        _threadSlider.setSnapToTicks(true);
        c.insets.left = 10;
        c.insets.top = 0;
        c.insets.right = 10;
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 1, 0, 1, 1, 0.5, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        threadPanelLayout.setConstraints(_threadSlider, c);
        threadPanel.add(_threadSlider);

        final NumberedLabel backupLevelLabel =
            new NumberedLabel(
                this.settings.getInt(
                    ConventionKeys.THREAD_COUNT, ConventionDefaults.THREAD_COUNT),
                this.bundle.getString("LBL_THREADS_SINGULAR" /* NOI18N */),
                this.bundle.getString("LBL_THREADS_PLURAL" /* NOI18N */));
        _threadSlider.addChangeListener(
            new ChangeListener()
            {
                public void stateChanged(ChangeEvent ev)
                {
                    JSlider source = (JSlider) ev.getSource();
                    int level = source.getValue();
                    backupLevelLabel.setLevel(level);
                }
            });
        c.insets.left = 0;
        c.insets.right = 15;
        SwingHelper.setConstraints(
            c, 2, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        threadPanelLayout.setConstraints(backupLevelLabel, c);
        threadPanel.add(backupLevelLabel);

        return threadPanel;
    }


    private void initialize()
    {
        JPanel removePanel = new JPanel();
        GridBagLayout removeLayout = new GridBagLayout();
        removePanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_MISC" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        removePanel.setLayout(removeLayout);

        GridBagConstraints c = new GridBagConstraints();

        _insertParenCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INSERT_PARENTHESES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INSERT_EXPRESSION_PARENTHESIS,
                    ConventionDefaults.INSERT_EXPRESSION_PARENTHESIS));
        _insertParenCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_insertParenCheckBox, c);
        removePanel.add(_insertParenCheckBox);

        _insertUIDCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INSERT_SERIAL_UID" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INSERT_SERIAL_UID, ConventionDefaults.INSERT_SERIAL_UID));
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_insertUIDCheckBox, c);
        removePanel.add(_insertUIDCheckBox);

        _insertConditionalCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INSERT_LOGGING_CONDITIONAL" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INSERT_LOGGING_CONDITIONAL,
                    ConventionDefaults.INSERT_LOGGING_CONDITIONAL));
        _insertConditionalCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_insertConditionalCheckBox, c);
        removePanel.add(_insertConditionalCheckBox);

        _insertTrailingNewlineCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INSERT_TRAILING_NEWLINE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INSERT_TRAILING_NEWLINE,
                    ConventionDefaults.INSERT_TRAILING_NEWLINE));
        _insertTrailingNewlineCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_insertTrailingNewlineCheckBox, c);
        removePanel.add(_insertTrailingNewlineCheckBox);

        _arrayBracketsAfterIdentifierCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BRACKETS_AFTER_IDENTIFIER" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ARRAY_BRACKETS_AFTER_IDENT,
                    ConventionDefaults.ARRAY_BRACKETS_AFTER_IDENT));
        _arrayBracketsAfterIdentifierCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_arrayBracketsAfterIdentifierCheckBox, c);
        removePanel.add(_arrayBracketsAfterIdentifierCheckBox);

        _forceCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_FORCE_FORMATTING" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.FORCE_FORMATTING, ConventionDefaults.FORCE_FORMATTING));
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_forceCheckBox, c);
        removePanel.add(_forceCheckBox);

        _insertMethodFinalModifierCheckBox =
            new JCheckBox(
                "Insert final for method params",
                this.settings.getBoolean(
                    ConventionKeys.INSERT_FINAL_MODIFIER_FOR_METHOD_PARAMETERS, ConventionDefaults.INSERT_FINAL_MODIFIER_FOR_METHOD_PARAMETERS));
        SwingHelper.setConstraints(
            c, 0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_insertMethodFinalModifierCheckBox, c);
        removePanel.add(_insertMethodFinalModifierCheckBox);
        _insertMethodFinalModifierCheckBox.addActionListener(trigger);

        _insertFinalModifierCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INSERT_FINAL_MODIFIER" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INSERT_FINAL_MODIFIER_FOR_PARAMETERS, ConventionDefaults.INSERT_FINAL_MODIFIER_FOR_PARAMETERS));
        SwingHelper.setConstraints(
            c, 1, 3, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        removeLayout.setConstraints(_insertFinalModifierCheckBox, c);
        removePanel.add(_insertFinalModifierCheckBox);
        _insertFinalModifierCheckBox.addActionListener(trigger);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        c.insets.top = 10;
        c.insets.bottom = 10;
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(removePanel, c);
        add(removePanel);
        c.insets.top = 0;

        JPanel historyPanel = createHistoryPanel();
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(historyPanel, c);
        add(historyPanel);

        JPanel backupPanel = createBackupPanel();
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(backupPanel, c);
        add(backupPanel);

        JPanel threadPanel = createThreadPanel();
        SwingHelper.setConstraints(
            c, 0, 3, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(threadPanel, c);
        add(threadPanel);
    }

    //~ Inner Classes --------------------------------------------------------------------

    private static class NumberedLabel
        extends JLabel
    {
        String plural;
        String singular;
        int level;

        public NumberedLabel(
            int    level,
            String singular,
            String plural)
        {
            this.singular = singular;
            this.plural = plural;
            setLevel(level);
        }

        public void setLevel(int level)
        {
            this.level = level;
            super.setText(
                (level != 1) ? (level + (' ' + this.plural))
                             : (level + (' ' + this.singular)));
        }


        public void setText(String text)
        {
        }
    }
}
