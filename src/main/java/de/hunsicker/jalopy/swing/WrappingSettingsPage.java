/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.EmptyButtonGroup;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer wrapping settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class WrappingSettingsPage
    extends AbstractSettingsPage
{
    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _alignExpressionCheckBox;
    private JCheckBox _alignMethodCallParamsCheckBox;
    private JCheckBox _alignMethodCallParamsIfNestedCheckBox;
    private JCheckBox _alignParamsCheckBox;
    private JCheckBox _alignValuesCheckBox;
    private JCheckBox _wrapAfterAssignCheckBox;
    private JCheckBox _wrapAfterChainedCallCheckBox;
    private JCheckBox _wrapAfterCheckBox;
    private JCheckBox _wrapParamsHardCheckBox;
    private JCheckBox _wrapParamsDeepCheckBox;
    private JCheckBox _wrapAfterExtendsCheckBox;
    private JCheckBox _wrapAfterImplementsCheckBox;
    private JCheckBox _wrapAfterLeftParenCheckBox;
    private JCheckBox _wrapAfterThrowsTypesCheckBox;
    private JCheckBox _wrapAllExtendsTypesExceedCheckBox;
    private JCheckBox _wrapAllImplementsTypesExceedCheckBox;
    private JCheckBox _wrapAllParamIfFirstCheckBox;
    private JCheckBox _wrapAllThrowsTypesIfExceedCheckBox;
    private JCheckBox _wrapBeforeCheckBox;
    private JCheckBox _wrapBeforeExtendsCheckBox;
    private JCheckBox _wrapBeforeImplementsCheckBox;
    private JCheckBox _wrapBeforeRightParenCheckBox;
    private JCheckBox _wrapBeforeThrowsCheckBox;
    private JCheckBox _wrapGroupingParenCheckBox;
    private JCheckBox _wrapLabelsCheckBox;
    private JCheckBox _wrapLinesCheckBox;
    private JComboBox _indentDeepComboBox;
    private JComboBox _lineLengthComboBox;
    private JTabbedPane _tabbedPane;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new WrappingSettingsPage object.
     */
    public WrappingSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new WrappingSettingsPage.
     *
     * @param container the parent container.
     */
    WrappingSettingsPage(SettingsContainer container)
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
                return "wrappingmisc" /* NOI18N */;

            default :
                return super.getPreviewFileName();
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updateSettings()
    {
        this.settings.putBoolean(
                                 ConventionKeys.LINE_WRAP, _wrapLinesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_BEFORE_OPERATOR, _wrapBeforeCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_LABEL, _wrapLabelsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_PAREN_GROUPING,
            _wrapGroupingParenCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.LINE_LENGTH, (String) _lineLengthComboBox.getSelectedItem());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_TERNARY_EXPRESSION, _alignExpressionCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_TERNARY_VALUES, _alignValuesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_BEFORE_THROWS, _wrapBeforeThrowsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS,
            _wrapAfterThrowsTypesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS,
            _wrapAfterImplementsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS,
            _wrapAfterExtendsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL,
            _alignMethodCallParamsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_ASSIGN, _wrapAfterAssignCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_PARAMS_EXCEED,
            _wrapAllParamIfFirstCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN,
            _wrapAfterLeftParenCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_BEFORE_RIGHT_PAREN,
            _wrapBeforeRightParenCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED,
            _alignMethodCallParamsIfNestedCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_DEF,
            _alignParamsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_BEFORE_EXTENDS,
            _wrapBeforeExtendsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_BEFORE_IMPLEMENTS,
            _wrapBeforeImplementsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_CHAINED_METHOD_CALL,
            _wrapAfterChainedCallCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_DEEP,
            (String) _indentDeepComboBox.getSelectedItem());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS_EXCEED,
            _wrapAllExtendsTypesExceedCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED,
            _wrapAllImplementsTypesExceedCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS_EXCEED,
            _wrapAllThrowsTypesIfExceedCheckBox.isSelected());

        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_PARAMS_HARD,
            _wrapParamsHardCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.LINE_WRAP_PARAMS_DEEP,
            _wrapParamsDeepCheckBox.isSelected());

        arraysPanel.setSettings(ConventionKeys.LINE_WRAP_ARRAY_ELEMENTS,ConventionKeys.LINE_WRAP_ARRAY_ELEMENTS);

        enumPanel.setSettings(ConventionKeys.ENUM_LCURLY_NO_NEW_LINE,ConventionKeys.ENUM_ALIGN_VALUES_WHEN_EXCEEDS);

        annotationPanel.setSettings(ConventionKeys.ANON_LCURLY_NO_NEW_LINE,ConventionKeys.ANON_ALIGN_VALUES_WHEN_EXCEEDS);
        annotationDefPanel.setSettings(ConventionKeys.ANON_DEF_LCURLY_NO_NEW_LINE,ConventionKeys.ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS);
    }

    private JPanel createAlwaysPane()
    {
        GridBagConstraints c = new GridBagConstraints();
        JPanel wrapAlwaysPanel = new JPanel();
        GridBagLayout wrapAlwaysPanelLayout = new GridBagLayout();

        wrapAlwaysPanel.setLayout(wrapAlwaysPanelLayout);
        wrapAlwaysPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_WRAP_ALWAYS" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _wrapBeforeExtendsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BEFORE_EXTENDS_KEYWORD" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_EXTENDS,
                    ConventionDefaults.LINE_WRAP_BEFORE_EXTENDS));
        _wrapBeforeExtendsCheckBox.addActionListener(this.trigger);
        c.insets.left = 0;
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapBeforeExtendsCheckBox, c);
        wrapAlwaysPanel.add(_wrapBeforeExtendsCheckBox);

        _wrapAfterExtendsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_AFTER_EXTENDS_TYPES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_EXTENDS));
        _wrapAfterExtendsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapAfterExtendsCheckBox, c);
        wrapAlwaysPanel.add(_wrapAfterExtendsCheckBox);

        _wrapBeforeImplementsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BEFORE_IMPLEMENTS_KEYWORD" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_IMPLEMENTS,
                    ConventionDefaults.LINE_WRAP_BEFORE_IMPLEMENTS));
        _wrapBeforeImplementsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapBeforeImplementsCheckBox, c);
        wrapAlwaysPanel.add(_wrapBeforeImplementsCheckBox);

        _wrapAfterImplementsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_AFTER_IMPLEMENTS_TYPES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_IMPLEMENTS));
        _wrapAfterImplementsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapAfterImplementsCheckBox, c);
        wrapAlwaysPanel.add(_wrapAfterImplementsCheckBox);

        _wrapBeforeThrowsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BEFORE_THROWS_KEYWORD" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_THROWS,
                    ConventionDefaults.LINE_WRAP_BEFORE_THROWS));
        _wrapBeforeThrowsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapBeforeThrowsCheckBox, c);
        wrapAlwaysPanel.add(_wrapBeforeThrowsCheckBox);

        _wrapAfterThrowsTypesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_AFTER_THROWS_TYPES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_THROWS));
        _wrapAfterThrowsTypesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapAfterThrowsTypesCheckBox, c);
        wrapAlwaysPanel.add(_wrapAfterThrowsTypesCheckBox);

        _alignParamsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_PARAM" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_DEF,
                    ConventionDefaults.LINE_WRAP_AFTER_PARAMS_METHOD_DEF));
        _alignParamsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_alignParamsCheckBox, c);
        wrapAlwaysPanel.add(_alignParamsCheckBox);

        _wrapAfterChainedCallCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_CALL_CHAINED" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_CHAINED_METHOD_CALL,
                    ConventionDefaults.LINE_WRAP_AFTER_CHAINED_METHOD_CALL));
        _wrapAfterChainedCallCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 3, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapAfterChainedCallCheckBox, c);
        wrapAlwaysPanel.add(_wrapAfterChainedCallCheckBox);

        _alignMethodCallParamsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_CALL_PARAM" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL,
                    ConventionDefaults.LINE_WRAP_AFTER_PARAMS_METHOD_CALL));
        _alignMethodCallParamsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_alignMethodCallParamsCheckBox, c);
        wrapAlwaysPanel.add(_alignMethodCallParamsCheckBox);

        _alignMethodCallParamsIfNestedCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_CALL_PARAM_NESTED" /* NOI18N */),
                !_alignMethodCallParamsCheckBox.isSelected()
                && this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED,
                    ConventionDefaults.LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED));
        _alignMethodCallParamsIfNestedCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 4, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_alignMethodCallParamsIfNestedCheckBox, c);
        wrapAlwaysPanel.add(_alignMethodCallParamsIfNestedCheckBox);

        EmptyButtonGroup group = new EmptyButtonGroup();
        group.add(_alignMethodCallParamsCheckBox);
        group.add(_alignMethodCallParamsIfNestedCheckBox);

        _alignExpressionCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_TERNARY_EXPRESSION_QUESTIONMARK" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_TERNARY_EXPRESSION,
                    ConventionDefaults.ALIGN_TERNARY_EXPRESSION));
        _alignExpressionCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_alignExpressionCheckBox, c);
        wrapAlwaysPanel.add(_alignExpressionCheckBox);

        _alignValuesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_TERNARY_EXPRESSION_COLON" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_TERNARY_VALUES,
                    ConventionDefaults.ALIGN_TERNARY_VALUES));
        _alignValuesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 5, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_alignValuesCheckBox, c);
        wrapAlwaysPanel.add(_alignValuesCheckBox);

        _wrapLabelsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_LABELS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_LABEL,
                    ConventionDefaults.LINE_WRAP_AFTER_LABEL));
        _wrapLabelsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 6, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAlwaysPanelLayout.setConstraints(_wrapLabelsCheckBox, c);
        wrapAlwaysPanel.add(_wrapLabelsCheckBox);

        JPanel wrapAllPanel = new JPanel();
        GridBagLayout wrapAllPanelLayout = new GridBagLayout();
        wrapAllPanel.setLayout(wrapAllPanelLayout);
        wrapAllPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_WRAP_ALL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _wrapAllExtendsTypesExceedCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_ALL_EXTENDS_TYPES_IF_FIRST" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS_EXCEED,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_EXTENDS_EXCEED));
        _wrapAllExtendsTypesExceedCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAllPanelLayout.setConstraints(_wrapAllExtendsTypesExceedCheckBox, c);
        wrapAllPanel.add(_wrapAllExtendsTypesExceedCheckBox);

        _wrapAllImplementsTypesExceedCheckBox =
            new JCheckBox(
                this.bundle.getString(
                    "CHK_WRAP_ALL_IMPLEMENTS_TYPES_IF_FIRST" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED));
        _wrapAllImplementsTypesExceedCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAllPanelLayout.setConstraints(_wrapAllImplementsTypesExceedCheckBox, c);
        wrapAllPanel.add(_wrapAllImplementsTypesExceedCheckBox);

        _wrapAllThrowsTypesIfExceedCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_ALL_THROWS_TYPES_IF_FIRST" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS_EXCEED,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_THROWS_EXCEED));
        _wrapAllThrowsTypesIfExceedCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAllPanelLayout.setConstraints(_wrapAllThrowsTypesIfExceedCheckBox, c);
        wrapAllPanel.add(_wrapAllThrowsTypesIfExceedCheckBox);

        _wrapAllParamIfFirstCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_ALL_PARAM_IF_FIRST" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_PARAMS_EXCEED,
                    ConventionDefaults.LINE_WRAP_PARAMS_EXCEED));
        _wrapAllParamIfFirstCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapAllPanelLayout.setConstraints(_wrapAllParamIfFirstCheckBox, c);
        wrapAllPanel.add(_wrapAllParamIfFirstCheckBox);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        c.insets.bottom = 10;
        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(wrapAlwaysPanel, c);
        panel.add(wrapAlwaysPanel);

        c.insets.top = 0;

        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(wrapAllPanel, c);
        panel.add(wrapAllPanel);

        return panel;
    }


    private JPanel createGeneralPane()
    {
        JPanel generalPanel = new JPanel();
        GridBagLayout generalPanelLayout = new GridBagLayout();
        generalPanel.setLayout(generalPanelLayout);
        generalPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_GENERAL" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagConstraints c = new GridBagConstraints();
        _wrapLinesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_LINES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP));
        _wrapLinesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 10, 0);
        generalPanelLayout.setConstraints(_wrapLinesCheckBox, c);
        generalPanel.add(_wrapLinesCheckBox);

        String[] lengths = createItemList(new int[] { 70, 79, 80, 90, 100 });
        ;

        ComboBoxPanel lineLengthComboBoxPanel =
            new NumberComboBoxPanel(
                this.bundle.getString("CMB_LINE_LENGTH" /* NOI18N */), lengths,
                this.settings.get(
                    ConventionKeys.LINE_LENGTH,
                    String.valueOf(ConventionDefaults.LINE_LENGTH)));
        _lineLengthComboBox = lineLengthComboBoxPanel.getComboBox();
        _lineLengthComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
            c.insets, 0, 0);
        generalPanelLayout.setConstraints(lineLengthComboBoxPanel, c);
        generalPanel.add(lineLengthComboBoxPanel);

        Object[] deepIndentSizeItems =
            createItemList(new int[] { 50, 55, 60, 65, 70, 75 });
        ComboBoxPanel deepIndent =
            new NumberComboBoxPanel(
                this.bundle.getString("CMB_DEEP_INDENT" /* NOI18N */), deepIndentSizeItems,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_DEEP,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_DEEP)));
        _indentDeepComboBox = deepIndent.getComboBox();
        _indentDeepComboBox.addActionListener(this.trigger);
        c.insets.left = 10;
        SwingHelper.setConstraints(
            c, 2, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.EAST,
            GridBagConstraints.NONE, c.insets, 0, 0);
        generalPanelLayout.setConstraints(deepIndent, c);
        generalPanel.add(deepIndent);

        c.insets.left = 0;

        JPanel wrapPolicyPanel = new JPanel();
        GridBagLayout wrapPolicyPanelLayout = new GridBagLayout();
        wrapPolicyPanel.setLayout(wrapPolicyPanelLayout);
        wrapPolicyPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_WRAP_POLICY" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _wrapAfterLeftParenCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_AFTER_LEFT_PAREN" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN,
                    ConventionDefaults.LINE_WRAP_AFTER_LEFT_PAREN));
        _wrapAfterLeftParenCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapAfterLeftParenCheckBox, c);
        wrapPolicyPanel.add(_wrapAfterLeftParenCheckBox);

        _wrapBeforeRightParenCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_BEFORE_RIGHT_PAREN" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_RIGHT_PAREN,
                    ConventionDefaults.LINE_WRAP_BEFORE_RIGHT_PAREN));
        _wrapBeforeRightParenCheckBox.addActionListener(this.trigger);

        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapBeforeRightParenCheckBox, c);
        wrapPolicyPanel.add(_wrapBeforeRightParenCheckBox);

        _wrapAfterAssignCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_AFTER_ASSIGN" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_ASSIGN,
                    ConventionDefaults.LINE_WRAP_AFTER_ASSIGN));
        _wrapAfterAssignCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapAfterAssignCheckBox, c);
        wrapPolicyPanel.add(_wrapAfterAssignCheckBox);

        _wrapGroupingParenCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_GROUPING" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_PAREN_GROUPING,
                    ConventionDefaults.LINE_WRAP_PAREN_GROUPING));
        _wrapGroupingParenCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapGroupingParenCheckBox, c);
        wrapPolicyPanel.add(_wrapGroupingParenCheckBox);

        _wrapBeforeCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_BEFORE_OPERATOR" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_OPERATOR,
                    ConventionDefaults.LINE_WRAP_BEFORE_OPERATOR));
        _wrapBeforeCheckBox.addActionListener(this.trigger);

        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapBeforeCheckBox, c);
        wrapPolicyPanel.add(_wrapBeforeCheckBox);

        _wrapAfterCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_WRAP_AFTER_OPERATOR" /* NOI18N */),
                !this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_OPERATOR,
                    ConventionDefaults.LINE_WRAP_BEFORE_OPERATOR));
        _wrapAfterCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapAfterCheckBox, c);
        wrapPolicyPanel.add(_wrapAfterCheckBox);
        
        _wrapParamsHardCheckBox =
            new JCheckBox(
                "Deep wrap on params" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_PARAMS_HARD,
                    ConventionDefaults.LINE_WRAP_PARAMS_HARD));
        
        _wrapParamsHardCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 3, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapParamsHardCheckBox, c);
        wrapPolicyPanel.add(_wrapParamsHardCheckBox);
        _wrapParamsHardCheckBox.addActionListener(new AbstractAction(""){
            public void actionPerformed(ActionEvent e) {
                    _wrapParamsDeepCheckBox.setEnabled(_wrapParamsHardCheckBox.isSelected());
            }
        });
        
        _wrapParamsDeepCheckBox = 
            new JCheckBox(
                "When wrap use deep" /* NOI18N */,
                this.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_PARAMS_DEEP,
                    ConventionDefaults.LINE_WRAP_PARAMS_DEEP));
        _wrapParamsDeepCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 3, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        wrapPolicyPanelLayout.setConstraints(_wrapParamsDeepCheckBox, c);
        wrapPolicyPanel.add(_wrapParamsDeepCheckBox);

        ButtonGroup operatorButtonGroup = new ButtonGroup();
        operatorButtonGroup.add(_wrapBeforeCheckBox);
        operatorButtonGroup.add(_wrapAfterCheckBox);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        c.insets.bottom = 10;
        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(generalPanel, c);
        panel.add(generalPanel);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(wrapPolicyPanel, c);
        panel.add(wrapPolicyPanel);

        return panel;
    }


    private JPanel createMiscPane()
    {
        GridBagConstraints c = new GridBagConstraints();

        //JPanel arraysPanel = new JPanel();
        
        String[] items = createItemList(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        int arrayElements =
            this.settings.getInt(
                ConventionKeys.LINE_WRAP_ARRAY_ELEMENTS,
                ConventionDefaults.LINE_WRAP_ARRAY_ELEMENTS);
        
        arraysPanel = new ComboPanel(this.bundle.getString("BDR_ARRAYS" /* NOI18N */),
                                   this.bundle.getString("CHK_WRAP_AS_NEEDED" /* NOI18N */),
                                   this.bundle.getString("CHK_WRAP_AFTER_ELEMENT" /* NOI18N */),
                                   this.bundle.getString("CMB_NUMBER" /* NOI18N */),
                                   arrayElements,
                                   items,c
                                   );
        
        
        // Enum area
      arrayElements =
      this.settings.getInt(
          ConventionKeys.ENUM_ALIGN_VALUES_WHEN_EXCEEDS,
          ConventionDefaults.ENUM_ALIGN_VALUES_WHEN_EXCEEDS);
      
        enumPanel = new ComboPanel(this.bundle.getString("LBL_ENUM" /* NOI18N */),
                                              this.bundle.getString("LBL_LEFT_BRACES_NOT_NEW_LINE" /* NOI18N */),
                                              this.bundle.getString("LBL_LEFT_BRACES_NEW_LINE" /* NOI18N */),
                                              this.bundle.getString("CMB_NUMBER" /* NOI18N */),
                                              arrayElements,
                                              items,c
                                              );

        // Annotation area
        arrayElements =
        this.settings.getInt(
            ConventionKeys.ANON_ALIGN_VALUES_WHEN_EXCEEDS,
            ConventionDefaults.ANON_ALIGN_VALUES_WHEN_EXCEEDS);
        annotationPanel = new ComboPanel(this.bundle.getString("LBL_ANON" /* NOI18N */),
                                                this.bundle.getString("LBL_LEFT_BRACES_NOT_NEW_LINE" /* NOI18N */),
                                                this.bundle.getString("LBL_LEFT_BRACES_NEW_LINE" /* NOI18N */),
                                                this.bundle.getString("CMB_NUMBER" /* NOI18N */),
                                                arrayElements,
                                                items,c
                                                );

        // Annotation Definition area
        arrayElements =
        this.settings.getInt(
            ConventionKeys.ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS,
            ConventionDefaults.ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS);
        annotationDefPanel = new ComboPanel(this.bundle.getString("LBL_ANON_DEF" /* NOI18N */),
                                                this.bundle.getString("LBL_LEFT_BRACES_NOT_NEW_LINE" /* NOI18N */),
                                                this.bundle.getString("LBL_LEFT_BRACES_NEW_LINE" /* NOI18N */),
                                                this.bundle.getString("CMB_NUMBER" /* NOI18N */),
                                                arrayElements,
                                                items,c
                                                );


        // Back to main panel
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        c.insets.bottom = 10;
        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;

        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(arraysPanel, c);
        panel.add(arraysPanel);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(enumPanel, c);
        panel.add(enumPanel);
        
        c.insets.top = 0;
        SwingHelper.setConstraints(
           c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
           GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
       layout.setConstraints(annotationPanel, c);
       panel.add(annotationPanel);

       c.insets.top = 0;
       SwingHelper.setConstraints(
          c, 0, 3, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
      layout.setConstraints(annotationDefPanel, c);
      panel.add(annotationDefPanel);
                               
        return panel;
    }
    ComboPanel enumPanel = null;
    ComboPanel arraysPanel = null;
    ComboPanel annotationDefPanel = null;
    ComboPanel annotationPanel = null;
    class ComboPanel extends JPanel {
        /**
         * Sets the settings with the new values
         * 
         * @param checkbox
         * @param combobox
         */
        public void setSettings(Convention.Key checkbox, Convention.Key combobox) {
            if (checkbox == combobox) {
                if (checkBox.isSelected())
                {
                    WrappingSettingsPage.this.settings.put(checkbox,
                        String.valueOf(0));
                }
                
                else if (ncb.getCheckBox().isSelected())
                {
                    WrappingSettingsPage.this.settings.put(
                        checkbox,
                        (String) ncb.getComboBoxPanel().getComboBox().getSelectedItem());
                }
                else
                {
                    WrappingSettingsPage.this.settings.putInt(
                                                              checkbox, Integer.MAX_VALUE);
                }
                
            }
            else {
                if (ncb.getCheckBox().isSelected()) {
                WrappingSettingsPage.this.settings.put(
                                                       combobox,(String) ncb.getComboBoxPanel().getComboBox().getSelectedItem());
            }
            else
            {
                WrappingSettingsPage.this.settings.putInt(
                                                          combobox, Integer.MAX_VALUE);
            }
            WrappingSettingsPage.this.settings.putBoolean(checkbox, checkBox.isSelected());
            }
            
        }
        JCheckBox checkBox = null;
        NumberComboBoxPanelCheckBox ncb = null;
        public JCheckBox getCheckBox() { return checkBox;}
        public NumberComboBoxPanelCheckBox getNCB() { return ncb;}
        public ComboPanel(String borderName,String chkBoxName,String ncbName,String comboName,int arrayElements,String[]items, GridBagConstraints c) {
            GridBagLayout enumPanelLayout = new GridBagLayout();
            this.setLayout(enumPanelLayout);
            this.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(borderName),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
            
            checkBox = new JCheckBox(chkBoxName,arrayElements == 0 || arrayElements == Integer.MAX_VALUE);
            checkBox.addActionListener(WrappingSettingsPage.this.trigger);
            SwingHelper.setConstraints(
                                       c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
            enumPanelLayout.setConstraints(checkBox, c);
            this.add(checkBox);
            
            ncb =
                new NumberComboBoxPanelCheckBox(
                                                	ncbName,
                    (arrayElements < Integer.MAX_VALUE) && (arrayElements > 0),
                    comboName, items,
                    getWrapValue(arrayElements));
            ncb.getCheckBox().addActionListener(WrappingSettingsPage.this.trigger);
            ncb.getComboBoxPanel().getComboBox().addActionListener(WrappingSettingsPage.this.trigger);
            SwingHelper.setConstraints(
                c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
            enumPanelLayout.setConstraints(ncb, c);
            this.add(ncb);
            EmptyButtonGroup eButtonGroup = new EmptyButtonGroup();
            
            eButtonGroup.add(checkBox);
            eButtonGroup.add(ncb.getCheckBox());        
            }
        
        /**
         * Returns the value that should be displayed in the combo box.
         *
         * @param value the value as stored in the settings.
         *
         * @return string value.
         */
        private String getWrapValue(int value)
        {
            switch (value)
            {
                case 0 : // wrap as needed
                case Integer.MAX_VALUE : // never wrap
                    return "1" /* NOI18N */;

                default :
                    return String.valueOf(value);
            }
        }
    }


    private void initialize()
    {
        _tabbedPane = new JTabbedPane();
        _tabbedPane.add(
            createGeneralPane(), this.bundle.getString("TAB_GENERAL" /* NOI18N */));
        _tabbedPane.add(
            createAlwaysPane(), this.bundle.getString("TAB_ALWAYS" /* NOI18N */));
        _tabbedPane.add(createMiscPane(), this.bundle.getString("TAB_MISC" /* NOI18N */));

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
