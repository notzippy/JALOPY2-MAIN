/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer blank lines settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class BlankLinesSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EMPTY_STRING = "" /* NOI18N */.intern();

    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _blankLinesAfterLeftCurlyCheckBox;
    private JCheckBox _blankLinesBeforeRightCurlyCheckBox;
    JCheckBox _chunksByBlankLinesCheckBox;
    private JCheckBox _chunksByCommentsCheckBox;
    JCheckBox _keepBlankLinesCheckBox;
    private JCheckBox _separatorCheckBox;
    private JCheckBox _separatorRecursiveCheckBox;
    private JComboBox _blankLinesAfterLeftCurlyComboBox;
    private JComboBox _blankLinesBeforeRightCurlyComboBox;
    private JComboBox _fillCharacterComboBox;
    private JComboBox _keepBlankLinesComboBox;
    private JComboBox _linesAfterBlockComboBox;
    private JComboBox _linesAfterClassComboBox;
    private JComboBox _linesAfterDeclarationComboBox;
    private JComboBox _linesAfterImportComboBox;
    private JComboBox _linesAfterInterfaceComboBox;
    private JComboBox _linesAfterMethodComboBox;
    private JComboBox _linesAfterPackageComboBox;
    private JComboBox _linesBeforeBlockComboBox;
    private JComboBox _linesBeforeCaseComboBox;
    private JComboBox _linesBeforeControlComboBox;
    private JComboBox _linesBeforeDeclarationComboBox;
    private JComboBox _linesBeforeJavadocComboBox;
    private JComboBox _linesBeforeMultiLineComboBox;
    private JComboBox _linesBeforeSingleLineComboBox;
    private JTabbedPane _tabbedPane;
    private JTextField _classTextField;
    private JTextField _constructorTextField;
    private JTextField _instanceInitTextField;
    private JTextField _instanceVarTextField;
    private JTextField _enumVarTextField;
    private JTextField _annotationVarTextField;
    private JTextField _interfaceTextField;
    private JTextField _methodTextField;
    private JTextField _staticVarInitTextField;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new BlankLinesSettingsPage object.
     */
    public BlankLinesSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new BlankLinesSettingsPage.
     *
     * @param container the parent container.
     */
    BlankLinesSettingsPage(SettingsContainer container)
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
                return "separation" /* NOI18N */;

            case 2 :
                return "separationcomments" /* NOI18N */;

            default :
                return super.getPreviewFileName();
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_METHOD,
            (String) _linesAfterMethodComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_CLASS,
            (String) _linesAfterClassComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_INTERFACE,
            (String) _linesAfterInterfaceComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_IMPORT,
            (String) _linesAfterImportComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_PACKAGE,
            (String) _linesAfterPackageComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_DECLARATION,
            (String) _linesAfterDeclarationComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_DECLARATION,
            (String) _linesBeforeDeclarationComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE,
            (String) _linesBeforeSingleLineComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE,
            (String) _linesBeforeMultiLineComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_CASE_BLOCK,
            (String) _linesBeforeCaseComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_BLOCK,
            (String) _linesBeforeBlockComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_AFTER_BLOCK,
            (String) _linesAfterBlockComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_CONTROL,
            (String) _linesBeforeControlComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_JAVADOC,
            (String) _linesBeforeJavadocComboBox.getSelectedItem());
        this.settings.putBoolean(
            ConventionKeys.CHUNKS_BY_COMMENTS, _chunksByCommentsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.CHUNKS_BY_BLANK_LINES, _chunksByBlankLinesCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.SEPARATOR_STATIC_VAR_INIT, _staticVarInitTextField.getText());
        this.settings.put(
            ConventionKeys.SEPARATOR_INSTANCE_VAR, _instanceVarTextField.getText());
        this.settings.put(
            ConventionKeys.SEPARATOR_ENUM_INIT, _enumVarTextField.getText());
        this.settings.put(
            ConventionKeys.SEPARATOR_ANNOTATION_INIT, _annotationVarTextField.getText());
        this.settings.put(
            ConventionKeys.SEPARATOR_INSTANCE_INIT, _instanceInitTextField.getText());
        this.settings.put(ConventionKeys.SEPARATOR_CTOR, _constructorTextField.getText());
        this.settings.put(ConventionKeys.SEPARATOR_METHOD, _methodTextField.getText());
        this.settings.put(
            ConventionKeys.SEPARATOR_INTERFACE, _interfaceTextField.getText());
        this.settings.put(ConventionKeys.SEPARATOR_CLASS, _classTextField.getText());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_INSERT_SEPARATOR, _separatorCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.COMMENT_INSERT_SEPARATOR_RECURSIVE,
            _separatorRecursiveCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.SEPARATOR_FILL_CHARACTER,
            (String) _fillCharacterComboBox.getSelectedItem());

        if (_blankLinesAfterLeftCurlyCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.BLANK_LINES_AFTER_BRACE_LEFT,
                (String) _blankLinesAfterLeftCurlyComboBox.getSelectedItem());
        }
        else
        {
            // disable blank lines after brace feature
            this.settings.put(
                ConventionKeys.BLANK_LINES_AFTER_BRACE_LEFT, "-1" /* NOI18N */);
        }

        if (_blankLinesBeforeRightCurlyCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT,
                (String) _blankLinesBeforeRightCurlyComboBox.getSelectedItem());
        }
        else
        {
            // disable blank lines before brace feature
            this.settings.put(
                ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT, "-1" /* NOI18N */);
        }

        if (_keepBlankLinesCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.BLANK_LINES_KEEP_UP_TO,
                (String) _keepBlankLinesComboBox.getSelectedItem());
        }
        else
        {
            // disable keep blank lines feature
            this.settings.put(ConventionKeys.BLANK_LINES_KEEP_UP_TO, "0" /* NOI18N */);
        }
    }


    private JPanel createCommentsPane()
    {
        JPanel separatorPanel = new JPanel();
        GridBagLayout separatorPanelLayout = new GridBagLayout();
        separatorPanel.setLayout(separatorPanelLayout);
        separatorPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagConstraints c = new GridBagConstraints();

        _separatorCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_ADD_SEPARATOR_COMMENTS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_INSERT_SEPARATOR,
                    ConventionDefaults.COMMENT_INSERT_SEPARATOR));
        _separatorCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        separatorPanelLayout.setConstraints(_separatorCheckBox, c);
        separatorPanel.add(_separatorCheckBox);

        _separatorRecursiveCheckBox =
            new JCheckBox(
                this.bundle.getString(
                    "CHK_ADD_SEPARATOR_COMMENTS_FOR_INNER" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.COMMENT_INSERT_SEPARATOR_RECURSIVE,
                    ConventionDefaults.COMMENT_INSERT_SEPARATOR_RECURSIVE));
        _separatorRecursiveCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        separatorPanelLayout.setConstraints(_separatorRecursiveCheckBox, c);
        separatorPanel.add(_separatorRecursiveCheckBox);

        JPanel textPanel = new JPanel();
        textPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_DESCRIPTIONS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout textPanelLayout = new GridBagLayout();
        textPanel.setLayout(textPanelLayout);
        c.insets.right = 10;

        JLabel staticVarInitLabel =
            new JLabel(this.bundle.getString("LBL_STATIC_VARS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(staticVarInitLabel, c);
        textPanel.add(staticVarInitLabel);
        _staticVarInitTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_STATIC_VAR_INIT,
                    ConventionDefaults.SEPARATOR_STATIC_VAR_INIT), 30);
        c.insets.right = 0;
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_staticVarInitTextField, c);
        textPanel.add(_staticVarInitTextField);

        JLabel instanceVarLabel =
            new JLabel(this.bundle.getString("LBL_INSTANCE_VARS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(instanceVarLabel, c);
        textPanel.add(instanceVarLabel);
        _instanceVarTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_INSTANCE_VAR,
                    ConventionDefaults.SEPARATOR_INSTANCE_VAR), 30);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_instanceVarTextField, c);
        textPanel.add(_instanceVarTextField);

        JLabel instanceInitLabel =
            new JLabel(this.bundle.getString("LBL_INSTANCE_INITS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(instanceInitLabel, c);
        textPanel.add(instanceInitLabel);
        _instanceInitTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_INSTANCE_INIT,
                    ConventionDefaults.SEPARATOR_INSTANCE_INIT), 30);
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_instanceInitTextField, c);
        textPanel.add(_instanceInitTextField);

        JLabel constructorLabel =
            new JLabel(this.bundle.getString("LBL_CTORS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(constructorLabel, c);
        textPanel.add(constructorLabel);
        _constructorTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_CTOR, ConventionDefaults.SEPARATOR_CTOR), 30);
        SwingHelper.setConstraints(
            c, 1, 3, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_constructorTextField, c);
        textPanel.add(_constructorTextField);

        JLabel methodLabel =
            new JLabel(this.bundle.getString("LBL_METHODS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(methodLabel, c);
        textPanel.add(methodLabel);
        _methodTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_METHOD, ConventionDefaults.SEPARATOR_METHOD),
                30);
        SwingHelper.setConstraints(
            c, 1, 4, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_methodTextField, c);
        textPanel.add(_methodTextField);

        JLabel interfaceLabel =
            new JLabel(this.bundle.getString("LBL_INTERFACES" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(interfaceLabel, c);
        textPanel.add(interfaceLabel);
        _interfaceTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_INTERFACE,
                    ConventionDefaults.SEPARATOR_INTERFACE), 30);
        SwingHelper.setConstraints(
            c, 1, 5, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_interfaceTextField, c);
        textPanel.add(_interfaceTextField);

        JLabel classLabel = new JLabel(this.bundle.getString("LBL_CLASSES" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(classLabel, c);
        textPanel.add(classLabel);
        _classTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_CLASS, ConventionDefaults.SEPARATOR_CLASS),
                30);
        SwingHelper.setConstraints(
            c, 1, 6, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_classTextField, c);
        textPanel.add(_classTextField);
        
        JLabel enumLabel = new JLabel("Enumeration");
        SwingHelper.setConstraints(
            c, 0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(enumLabel, c);
        textPanel.add(enumLabel);
        _enumVarTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_ENUM_INIT, ConventionDefaults.SEPARATOR_ENUM_INIT),
                30);
        SwingHelper.setConstraints(
            c, 1, 7, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_enumVarTextField, c);
        textPanel.add(_enumVarTextField);

        
        JLabel anontationLabel = new JLabel("Annotation");
        SwingHelper.setConstraints(
            c, 0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        textPanelLayout.setConstraints(anontationLabel, c);
        textPanel.add(anontationLabel);
        _annotationVarTextField =
            new JTextField(
                this.settings.get(
                    ConventionKeys.SEPARATOR_ANNOTATION_INIT, ConventionDefaults.SEPARATOR_ANNOTATION_INIT),
                30);
        SwingHelper.setConstraints(
            c, 1, 8, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        textPanelLayout.setConstraints(_annotationVarTextField, c);
        textPanel.add(_annotationVarTextField);

        
        JPanel characterPanel = new JPanel();
        GridBagLayout characterPanelLayout = new GridBagLayout();
        characterPanel.setLayout(characterPanelLayout);
        characterPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_FILL_CHARACTER" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        Object[] items =
        {
            "\u00b7" /* NOI18N */, "." /* NOI18N */, "-" /* NOI18N */, "=" /* NOI18N */,
            "*" /* NOI18N */, "/" /* NOI18N */
        };
        ComboBoxPanel fillCharacterComboBoxPanel =
            new ComboBoxPanel(
                this.bundle.getString("LBL_CHARACTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.SEPARATOR_FILL_CHARACTER,
                    ConventionDefaults.SEPARATOR_FILL_CHARACTER));
        _fillCharacterComboBox = fillCharacterComboBoxPanel.getComboBox();
        _fillCharacterComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        characterPanelLayout.setConstraints(fillCharacterComboBoxPanel, c);
        characterPanel.add(fillCharacterComboBoxPanel);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        c.insets.top = 10;
        c.insets.bottom = 0;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(separatorPanel, c);
        panel.add(separatorPanel);

        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(textPanel, c);
        panel.add(textPanel);
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(characterPanel, c);
        panel.add(characterPanel);

        return panel;
    }


    private JPanel createGeneralPane()
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        GridBagLayout panelLayout = new GridBagLayout();
        panel.setLayout(panelLayout);

        Object[] items = createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
        JLabel packageLabel =
            new JLabel(this.bundle.getString("LBL_PACKAGE" /* NOI18N */));
        GridBagConstraints c = new GridBagConstraints();
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(packageLabel, c);
        panel.add(packageLabel);

        ComboBoxPanel afterPackage =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_PACKAGE,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_PACKAGE)));
        SwingHelper.setConstraints(
            c, 2, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterPackageComboBox = afterPackage.getComboBox();
        _linesAfterPackageComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterPackage, c);
        panel.add(afterPackage);

        JLabel importLabel =
            new JLabel(this.bundle.getString("LBL_LAST_IMPORT" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(importLabel, c);
        panel.add(importLabel);

        ComboBoxPanel afterImport =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_IMPORT,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_IMPORT)));
        SwingHelper.setConstraints(
            c, 2, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterImportComboBox = afterImport.getComboBox();
        _linesAfterImportComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterImport, c);
        panel.add(afterImport);

        JLabel classLabel = new JLabel(this.bundle.getString("LBL_CLASSES" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(classLabel, c);
        panel.add(classLabel);

        ComboBoxPanel afterClass =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_CLASS,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_CLASS)));
        SwingHelper.setConstraints(
            c, 2, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterClassComboBox = afterClass.getComboBox();
        _linesAfterClassComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterClass, c);
        panel.add(afterClass);

        JLabel interfaceLabel =
            new JLabel(this.bundle.getString("LBL_INTERFACES" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 3, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(interfaceLabel, c);
        panel.add(interfaceLabel);

        ComboBoxPanel afterInterface =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_INTERFACE,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_INTERFACE)));
        SwingHelper.setConstraints(
            c, 2, 3, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterInterfaceComboBox = afterInterface.getComboBox();
        _linesAfterInterfaceComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterInterface, c);
        panel.add(afterInterface);

        JLabel methodLabel =
            new JLabel(this.bundle.getString("LBL_METHODS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 4, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(methodLabel, c);
        panel.add(methodLabel);

        ComboBoxPanel afterMethod =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_METHOD,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_METHOD)));
        SwingHelper.setConstraints(
            c, 2, 4, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterMethodComboBox = afterMethod.getComboBox();
        _linesAfterMethodComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterMethod, c);
        panel.add(afterMethod);

        JLabel blockBeforeLabel =
            new JLabel(this.bundle.getString("LBL_BLOCKS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 5, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(blockBeforeLabel, c);
        panel.add(blockBeforeLabel);

        ComboBoxPanel beforeBlock =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_BLOCK,
                    String.valueOf(ConventionDefaults.BLANK_LINES_BEFORE_BLOCK)));
        SwingHelper.setConstraints(
            c, 1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        _linesBeforeBlockComboBox = beforeBlock.getComboBox();
        _linesBeforeBlockComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeBlock, c);
        panel.add(beforeBlock);

        ComboBoxPanel afterBlock =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_BLOCK,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_BLOCK)));
        SwingHelper.setConstraints(
            c, 2, 5, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterBlockComboBox = afterBlock.getComboBox();
        _linesAfterBlockComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterBlock, c);
        panel.add(afterBlock);

        JLabel declarationLabel =
            new JLabel(this.bundle.getString("LBL_DECLARATIONS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 6, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(declarationLabel, c);
        panel.add(declarationLabel);

        ComboBoxPanel beforeDeclaration =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_DECLARATION,
                    String.valueOf(ConventionDefaults.BLANK_LINES_BEFORE_DECLARATION)));
        SwingHelper.setConstraints(
            c, 1, 6, GridBagConstraints.REMAINDER, 1, 0.7, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesBeforeDeclarationComboBox = beforeDeclaration.getComboBox();
        _linesBeforeDeclarationComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeDeclaration, c);
        panel.add(beforeDeclaration);

        ComboBoxPanel afterDeclaration =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_AFTER" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_AFTER_DECLARATION,
                    String.valueOf(ConventionDefaults.BLANK_LINES_AFTER_DECLARATION)));
        SwingHelper.setConstraints(
            c, 2, 6, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesAfterDeclarationComboBox = afterDeclaration.getComboBox();
        _linesAfterDeclarationComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(afterDeclaration, c);
        panel.add(afterDeclaration);

        JLabel caseLabel =
            new JLabel(this.bundle.getString("LBL_CASE_BLOCKS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 7, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(caseLabel, c);
        panel.add(caseLabel);

        ComboBoxPanel beforeCase =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_CASE_BLOCK,
                    String.valueOf(ConventionDefaults.BLANK_LINES_BEFORE_CASE_BLOCK)));
        SwingHelper.setConstraints(
            c, 1, 7, GridBagConstraints.REMAINDER, 1, 0.7, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesBeforeCaseComboBox = beforeCase.getComboBox();
        _linesBeforeCaseComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeCase, c);
        panel.add(beforeCase);

        JLabel controlLabel =
            new JLabel(this.bundle.getString("LBL_CONTROL" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 8, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(controlLabel, c);
        panel.add(controlLabel);

        ComboBoxPanel beforeControlPanel =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_CONTROL,
                    String.valueOf(ConventionDefaults.BLANK_LINES_BEFORE_CONTROL)));
        SwingHelper.setConstraints(
            c, 1, 8, GridBagConstraints.REMAINDER, 1, 0.7, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesBeforeControlComboBox = beforeControlPanel.getComboBox();
        _linesBeforeControlComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeControlPanel, c);
        panel.add(beforeControlPanel);

        JLabel singleLineLabel =
            new JLabel(this.bundle.getString("LBL_SINGLE_LINE" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 9, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(singleLineLabel, c);
        panel.add(singleLineLabel);

        ComboBoxPanel beforeSingleLine =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE,
                    String.valueOf(
                        ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE)));
        SwingHelper.setConstraints(
            c, 1, 9, GridBagConstraints.REMAINDER, 1, 0.7, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        _linesBeforeSingleLineComboBox = beforeSingleLine.getComboBox();
        _linesBeforeSingleLineComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeSingleLine, c);
        panel.add(beforeSingleLine);

        JLabel multiLineLabel =
            new JLabel(this.bundle.getString("LBL_MULTI_LINE" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 10, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(multiLineLabel, c);
        panel.add(multiLineLabel);

        ComboBoxPanel beforeMultiLine =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE,
                    String.valueOf(
                        ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE)));
        SwingHelper.setConstraints(
            c, 1, 10, GridBagConstraints.REMAINDER, 1, 0.7, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
        _linesBeforeMultiLineComboBox = beforeMultiLine.getComboBox();
        _linesBeforeMultiLineComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeMultiLine, c);
        panel.add(beforeMultiLine);

        JLabel javadocLabel =
            new JLabel(this.bundle.getString("LBL_JAVADOC_COMMENTS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 0, 11, 1, 1, 0.3, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        panelLayout.setConstraints(javadocLabel, c);
        panel.add(javadocLabel);

        ComboBoxPanel beforeJavadocSettingsPage =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_BEFORE" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.BLANK_LINES_BEFORE_COMMENT_JAVADOC,
                    String.valueOf(ConventionDefaults.BLANK_LINES_BEFORE_COMMENT_JAVADOC)));
        SwingHelper.setConstraints(
            c, 1, 11, GridBagConstraints.REMAINDER, 1, 0.7, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
        _linesBeforeJavadocComboBox = beforeJavadocSettingsPage.getComboBox();
        _linesBeforeJavadocComboBox.addActionListener(this.trigger);
        panelLayout.setConstraints(beforeJavadocSettingsPage, c);
        panel.add(beforeJavadocSettingsPage);
        SwingHelper.setConstraints(
            c, 0, 12, GridBagConstraints.REMAINDER, 1, 0.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);

        Component glue = Box.createVerticalGlue();
        panelLayout.setConstraints(glue, c);
        panel.add(glue);

        return panel;
    }


    private JPanel createMiscPane()
    {
        JPanel arrayPanel = new JPanel();
        arrayPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_MISC" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout arrayLayout = new GridBagLayout();
        arrayPanel.setLayout(arrayLayout);

        String[] items = createItemList(new int[] { 0, 1, 2, 3, 4, 5 });
        int blankLinesAfterLeftCurly =
            this.settings.getInt(
                ConventionKeys.BLANK_LINES_AFTER_BRACE_LEFT,
                ConventionDefaults.BLANK_LINES_AFTER_BRACE_LEFT);
        NumberComboBoxPanelCheckBox blankLinesAfterLeftCurlyCheck =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("LBL_BLANK_LINES_AFTER_LEFT" /* NOI18N */),
                blankLinesAfterLeftCurly > -1, EMPTY_STRING, items,
                (blankLinesAfterLeftCurly > -1)
                ? String.valueOf(blankLinesAfterLeftCurly)
                : "0");
        _blankLinesAfterLeftCurlyCheckBox = blankLinesAfterLeftCurlyCheck.getCheckBox();
        _blankLinesAfterLeftCurlyCheckBox.addActionListener(this.trigger);
        _blankLinesAfterLeftCurlyComboBox =
            blankLinesAfterLeftCurlyCheck.getComboBoxPanel().getComboBox();
        _blankLinesAfterLeftCurlyComboBox.addActionListener(this.trigger);

        GridBagConstraints c = new GridBagConstraints();
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        arrayLayout.setConstraints(blankLinesAfterLeftCurlyCheck, c);
        arrayPanel.add(blankLinesAfterLeftCurlyCheck);

        int blankLinesBeforeRightCurly =
            this.settings.getInt(
                ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT,
                ConventionDefaults.BLANK_LINES_BEFORE_BRACE_RIGHT);
        NumberComboBoxPanelCheckBox blankLinesBeforeRightCurlyCheck =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("LBL_BLANK_LINES_BEFORE_RIGHT" /* NOI18N */),
                blankLinesBeforeRightCurly > -1, EMPTY_STRING, items,
                (blankLinesBeforeRightCurly > -1)
                ? String.valueOf(blankLinesBeforeRightCurly)
                : "0");
        _blankLinesBeforeRightCurlyCheckBox =
            blankLinesBeforeRightCurlyCheck.getCheckBox();
        _blankLinesBeforeRightCurlyComboBox =
            blankLinesBeforeRightCurlyCheck.getComboBoxPanel().getComboBox();
        _blankLinesBeforeRightCurlyCheckBox.addActionListener(this.trigger);
        _blankLinesBeforeRightCurlyComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        arrayLayout.setConstraints(blankLinesBeforeRightCurlyCheck, c);
        arrayPanel.add(blankLinesBeforeRightCurlyCheck);

        int keepBlankLines =
            this.settings.getInt(
                ConventionKeys.BLANK_LINES_KEEP_UP_TO,
                ConventionDefaults.BLANK_LINES_KEEP_UP_TO);
        String[] blankItems = createItemList(new int[] { 1, 2, 3, 4, 5 });
        NumberComboBoxPanelCheckBox keepBlankLinesCheck =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("LBL_KEEP_BLANK_LINES" /* NOI18N */),
                keepBlankLines > 0, EMPTY_STRING, blankItems,
                (keepBlankLines > 0) ? String.valueOf(keepBlankLines)
                                     : "1");
        _keepBlankLinesCheckBox = keepBlankLinesCheck.getCheckBox();
        _keepBlankLinesComboBox = keepBlankLinesCheck.getComboBoxPanel().getComboBox();
        _keepBlankLinesCheckBox.addActionListener(this.trigger);
        _keepBlankLinesComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        arrayLayout.setConstraints(keepBlankLinesCheck, c);
        arrayPanel.add(keepBlankLinesCheck);

        JPanel chunksPanel = new JPanel();
        chunksPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_CHUNKS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout chunksPanelLayout = new GridBagLayout();
        chunksPanel.setLayout(chunksPanelLayout);
        _chunksByCommentsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BY_COMMENTS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.CHUNKS_BY_COMMENTS,
                    ConventionDefaults.CHUNKS_BY_COMMENTS));
        _chunksByCommentsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        chunksPanelLayout.setConstraints(_chunksByCommentsCheckBox, c);
        chunksPanel.add(_chunksByCommentsCheckBox);

        _chunksByBlankLinesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BY_BLANK_LINES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.CHUNKS_BY_BLANK_LINES,
                    ConventionDefaults.CHUNKS_BY_BLANK_LINES));
        _chunksByBlankLinesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        chunksPanelLayout.setConstraints(_chunksByBlankLinesCheckBox, c);
        chunksPanel.add(_chunksByBlankLinesCheckBox);
        _chunksByBlankLinesCheckBox.setEnabled(_keepBlankLinesCheckBox.isSelected());

        GridBagLayout layout = new GridBagLayout();
        JPanel panel = new JPanel();
        panel.setLayout(layout);

        c.insets.top = 10;
        c.insets.bottom = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(arrayPanel, c);
        panel.add(arrayPanel);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(chunksPanel, c);
        panel.add(chunksPanel);
        _keepBlankLinesCheckBox.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    if (_keepBlankLinesCheckBox.isSelected())
                    {
                        _chunksByBlankLinesCheckBox.setEnabled(true);
                    }
                    else
                    {
                        _chunksByBlankLinesCheckBox.setEnabled(false);
                    }
                }
            });

        return panel;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        _tabbedPane = new JTabbedPane();
        _tabbedPane.add(
            createGeneralPane(), this.bundle.getString("TAB_GENERAL" /* NOI18N */));
        _tabbedPane.add(createMiscPane(), this.bundle.getString("TAB_MISC" /* NOI18N */));
        _tabbedPane.add(
            createCommentsPane(), this.bundle.getString("TAB_COMMENTS" /* NOI18N */));

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
}
