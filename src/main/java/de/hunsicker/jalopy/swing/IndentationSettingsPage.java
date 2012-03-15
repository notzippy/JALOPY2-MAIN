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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer indentation settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class IndentationSettingsPage
    extends AbstractSettingsPage
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EMPTY_STRING = "" /* NOI18N */.intern();
    private static final String NONE = "-1" /* NOI18N */;
    private static final String ZERO = "0" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    //private JCheckBox _indentContinuationTernaryCheckBox;
    private JCheckBox _alignAssignmentsCheckBox;
    private JCheckBox _alignDeclAssignmentsCheckBox;
    private JCheckBox _alignMethodCallChainsCheckBox;
    private JCheckBox _alignMethodDefParamsCheckBox;
    private JCheckBox _alignTernaryOperatorCheckBox;
    private JCheckBox _alignVariablesCheckBox;
    private JCheckBox _indentCaseSwitchCheckBox;
    private JCheckBox _indentContinuationCheckBox;
    private JCheckBox _indentContinuationOperatorCheckBox;
    private JCheckBox _indentExtendsCheckBox;
    private JCheckBox _indentFirstColumnCheckBox;
    private JCheckBox _indentImplementsCheckBox;
    private JCheckBox _indentLabelsCheckBox;
//TODO    private JCheckBox _indentMethodCallCheckBox;
//TODO    private JCheckBox _indentParametersCheckBox;
    private JCheckBox _indentThrowsCheckBox;
    private JCheckBox _indentUsingLeadingTabsCheckBox;
    private JCheckBox _indentUsingTabsCheckBox;
    private JCheckBox _standardIndentCheckBox;
    private JComboBox _continuationIndentComboBox;
    private JComboBox _endlineIndentComboBox;
    private JComboBox _indentComboBox;
    private JComboBox _indentExtendsComboBox;
    private JComboBox _indentImplementsComboBox;
    private JComboBox _indentThrowsComboBox;
    private JComboBox _leadingIndentComboBox;
    private JComboBox _tabSizeComboBox;
    private JTabbedPane _tabbedPane;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new IndentationSettingsPage object.
     */
    public IndentationSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new IndentationSettingsPage.
     *
     * @param container the parent container.
     */
    IndentationSettingsPage(SettingsContainer container)
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
                return "indentationmisc" /* NOI18N */;

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
            ConventionKeys.INDENT_SIZE, (String) _indentComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_LEADING,
            (String) _leadingIndentComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_CONTINUATION,
            (String) _continuationIndentComboBox.getSelectedItem());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_COMMENT_ENDLINE,
            (String) _endlineIndentComboBox.getSelectedItem());
        this.settings.putBoolean(
            ConventionKeys.INDENT_CASE_FROM_SWITCH, _indentCaseSwitchCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INDENT_LABEL, _indentLabelsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INDENT_CONTINUATION_BLOCK,
            _indentContinuationCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INDENT_FIRST_COLUMN_COMMENT,
            _indentFirstColumnCheckBox.isSelected());
        this.settings.put(
            ConventionKeys.INDENT_SIZE_TABS, (String) _tabSizeComboBox.getSelectedItem());
        this.settings.putBoolean(
            ConventionKeys.INDENT_WITH_TABS, _indentUsingTabsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INDENT_WITH_TABS_ONLY_LEADING,
            _indentUsingLeadingTabsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_VAR_IDENTS, _alignVariablesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_VAR_ASSIGNS, _alignAssignmentsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_VAR_DECL_ASSIGNS, _alignDeclAssignmentsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_PARAMS_METHOD_DEF,
            _alignMethodDefParamsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_TERNARY_OPERATOR,
            _alignTernaryOperatorCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INDENT_CONTINUATION_OPERATOR,
            _indentContinuationOperatorCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.INDENT_DEEP, !_standardIndentCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.ALIGN_METHOD_CALL_CHAINS,
            _alignMethodCallChainsCheckBox.isSelected());

        if (_indentExtendsCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.INDENT_SIZE_EXTENDS,
                (String) _indentExtendsComboBox.getSelectedItem());
        }
        else
        {
            this.settings.put(ConventionKeys.INDENT_SIZE_EXTENDS, NONE);
        }

        if (_indentImplementsCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.INDENT_SIZE_IMPLEMENTS,
                (String) _indentImplementsComboBox.getSelectedItem());
        }
        else
        {
            this.settings.put(ConventionKeys.INDENT_SIZE_IMPLEMENTS, NONE);
        }

        if (_indentThrowsCheckBox.isSelected())
        {
            this.settings.put(
                ConventionKeys.INDENT_SIZE_THROWS,
                (String) _indentThrowsComboBox.getSelectedItem());
        }
        else
        {
            this.settings.put(ConventionKeys.INDENT_SIZE_THROWS, NONE);
        }
    }


    private JPanel createGeneralPane()
    {
        JPanel policyPanel = new JPanel();
        policyPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_INDENTATION_POLICY" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout policyPanelLayout = new GridBagLayout();
        policyPanel.setLayout(policyPanelLayout);

        GridBagConstraints c = new GridBagConstraints();

        _standardIndentCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_STANDARD_INDENT" /* NOI18N */),
                !this.settings.getBoolean(
                    ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP));
        _standardIndentCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        policyPanelLayout.setConstraints(_standardIndentCheckBox, c);
        policyPanel.add(_standardIndentCheckBox);

        JCheckBox deepIndentCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_DEEP_INDENT" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP));
        deepIndentCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        policyPanelLayout.setConstraints(deepIndentCheckBox, c);
        policyPanel.add(deepIndentCheckBox);

        ButtonGroup policyGroup = new ButtonGroup();
        policyGroup.add(_standardIndentCheckBox);
        policyGroup.add(deepIndentCheckBox);

        JPanel indentPanel = new JPanel();
        indentPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_SIZES" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagLayout indentPanelLayout = new GridBagLayout();
        indentPanel.setLayout(indentPanelLayout);

        Object[] items = createItemList(new int[] { 2, 3, 4 });
        ComboBoxPanel indent =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_GENERAL_INDENT" /* NOI18N */), items,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE,
                    String.valueOf(ConventionDefaults.INDENT_SIZE)));
        _indentComboBox = indent.getComboBox();
        _indentComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(indent, c);
        indentPanel.add(indent);

        Object[] leadingItems = createItemList(new int[] { 0, 2, 4, 6, 8, 10 });
        ComboBoxPanel leadingIndent =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_LEADING_INDENT" /* NOI18N */), leadingItems,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_LEADING,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_LEADING)));
        _leadingIndentComboBox = leadingIndent.getComboBox();
        _leadingIndentComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(leadingIndent, c);
        indentPanel.add(leadingIndent);

        Object[] continuationItems = createItemList(new int[] { 2, 4, 6, 8, 10, 12 });
        ComboBoxPanel continuationIndent =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_CONTINUATION_INDENT" /* NOI18N */),
                continuationItems,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_CONTINUATION,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_CONTINUATION)));
        _continuationIndentComboBox = continuationIndent.getComboBox();
        _continuationIndentComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(continuationIndent, c);
        indentPanel.add(continuationIndent);

        Object[] endlineItems = createItemList(new int[] { 0, 1, 2, 3, 4 });
        ComboBoxPanel endlineIndent =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_TRAILING_INDENT" /* NOI18N */), endlineItems,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_COMMENT_ENDLINE,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_COMMENT_ENDLINE)));
        _endlineIndentComboBox = endlineIndent.getComboBox();
        _endlineIndentComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 3, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(endlineIndent, c);
        indentPanel.add(endlineIndent);

        Object[] tabSizeItems = createItemList(new int[] { 2, 3, 4, 6, 8, 10 });
        ComboBoxPanel tabSize =
            new NumberComboBoxPanel(
                this.bundle.getString("LBL_ORIGINAL_TAB" /* NOI18N */), tabSizeItems,
                this.settings.get(
                    ConventionKeys.INDENT_SIZE_TABS,
                    String.valueOf(ConventionDefaults.INDENT_SIZE_TABS)));
        _tabSizeComboBox = tabSize.getComboBox();
        _tabSizeComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 4, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(tabSize, c);
        indentPanel.add(tabSize);

        String[] identItems = createItemList(new int[] { 0, 2, 3, 4, 6, 8 });
        int indentExtends =
            this.settings.getInt(
                ConventionKeys.INDENT_SIZE_EXTENDS, ConventionDefaults.INDENT_SIZE_EXTENDS);
        NumberComboBoxPanelCheckBox indentExtendsComboCheckBox =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("CHK_EXTENDS_INDENT" /* NOI18N */),
                indentExtends > -1, EMPTY_STRING, identItems,
                (indentExtends > -1) ? String.valueOf(indentExtends)
                                     : ZERO);
        _indentExtendsCheckBox = indentExtendsComboCheckBox.getCheckBox();
        _indentExtendsCheckBox.addActionListener(this.trigger);
        _indentExtendsComboBox =
            indentExtendsComboCheckBox.getComboBoxPanel().getComboBox();
        _indentExtendsComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 5, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(indentExtendsComboCheckBox, c);
        indentPanel.add(indentExtendsComboCheckBox);

        int indentImplements =
            this.settings.getInt(
                ConventionKeys.INDENT_SIZE_IMPLEMENTS,
                ConventionDefaults.INDENT_SIZE_IMPLEMENTS);
        NumberComboBoxPanelCheckBox indentImplementsComboCheckBox =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("CHK_IMPLEMENTS_INDENT" /* NOI18N */),
                indentImplements > -1, EMPTY_STRING, identItems,
                (indentImplements > -1) ? String.valueOf(indentImplements)
                                        : ZERO);
        _indentImplementsCheckBox = indentImplementsComboCheckBox.getCheckBox();
        _indentImplementsCheckBox.addActionListener(this.trigger);
        _indentImplementsComboBox =
            indentImplementsComboCheckBox.getComboBoxPanel().getComboBox();
        _indentImplementsComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 6, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(indentImplementsComboCheckBox, c);
        indentPanel.add(indentImplementsComboCheckBox);

        int indentThrows =
            this.settings.getInt(
                ConventionKeys.INDENT_SIZE_THROWS, ConventionDefaults.INDENT_SIZE_THROWS);
        NumberComboBoxPanelCheckBox indentThrowsComboCheckBox =
            new NumberComboBoxPanelCheckBox(
                this.bundle.getString("CHK_THROWS_INDENT" /* NOI18N */), indentThrows > -1,
                EMPTY_STRING, identItems,
                (indentThrows > -1) ? String.valueOf(indentThrows)
                                    : ZERO);
        _indentThrowsCheckBox = indentThrowsComboCheckBox.getCheckBox();
        _indentThrowsComboBox =
            indentThrowsComboCheckBox.getComboBoxPanel().getComboBox();
        _indentThrowsCheckBox.addActionListener(this.trigger);
        _indentThrowsComboBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 7, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        indentPanelLayout.setConstraints(indentThrowsComboCheckBox, c);
        indentPanel.add(indentThrowsComboCheckBox);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);

        c.insets.bottom = 0;
        c.insets.top = 10;
        c.insets.left = 5;
        c.insets.right = 5;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(policyPanel, c);
        panel.add(policyPanel);

        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(indentPanel, c);
        panel.add(indentPanel);

        return panel;
    }


    private JPanel createMiscPane()
    {
        JPanel miscPanel = new JPanel();
        GridBagLayout miscPanelLayout = new GridBagLayout();

        miscPanel.setLayout(miscPanelLayout);
        miscPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_MISC" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        GridBagConstraints c = new GridBagConstraints();
        _indentUsingTabsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_USE_TABS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_WITH_TABS, ConventionDefaults.INDENT_WITH_TABS));
        _indentUsingTabsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPanelLayout.setConstraints(_indentUsingTabsCheckBox, c);
        miscPanel.add(_indentUsingTabsCheckBox);

        _indentUsingLeadingTabsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_USE_LEADING_TABS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_WITH_TABS_ONLY_LEADING,
                    ConventionDefaults.INDENT_WITH_TABS_ONLY_LEADING));
        _indentUsingLeadingTabsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPanelLayout.setConstraints(_indentUsingLeadingTabsCheckBox, c);
        miscPanel.add(_indentUsingLeadingTabsCheckBox);

        _indentCaseSwitchCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INDENT_CASE_SWITCH" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_CASE_FROM_SWITCH,
                    ConventionDefaults.INDENT_CASE_FROM_SWITCH));
        _indentCaseSwitchCheckBox.addActionListener(this.trigger);
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPanelLayout.setConstraints(_indentCaseSwitchCheckBox, c);
        miscPanel.add(_indentCaseSwitchCheckBox);

        _indentLabelsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INDENT_LABELS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_LABEL, ConventionDefaults.INDENT_LABEL));
        _indentLabelsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPanelLayout.setConstraints(_indentLabelsCheckBox, c);
        miscPanel.add(_indentLabelsCheckBox);

        _indentFirstColumnCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_INDENT_FIRST_COLUMN" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_FIRST_COLUMN_COMMENT,
                    ConventionDefaults.INDENT_FIRST_COLUMN_COMMENT));
        _indentFirstColumnCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        miscPanelLayout.setConstraints(_indentFirstColumnCheckBox, c);
        miscPanel.add(_indentFirstColumnCheckBox);

        JPanel continuationPanel = new JPanel();
        GridBagLayout continuationPanelLayout = new GridBagLayout();

        continuationPanel.setLayout(continuationPanelLayout);
        continuationPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_CONTINUATION_INDENT" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _indentContinuationCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_CONTINUATION_INDENT_BLOCKS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_CONTINUATION_BLOCK,
                    ConventionDefaults.INDENT_CONTINUATION_BLOCK));
        _indentContinuationCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        continuationPanelLayout.setConstraints(_indentContinuationCheckBox, c);
        continuationPanel.add(_indentContinuationCheckBox);

        _indentContinuationOperatorCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_CONTINUATION_INDENT_OPERATORS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.INDENT_CONTINUATION_OPERATOR,
                    ConventionDefaults.INDENT_CONTINUATION_OPERATOR));
        _indentContinuationOperatorCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        continuationPanelLayout.setConstraints(_indentContinuationOperatorCheckBox, c);
        continuationPanel.add(_indentContinuationOperatorCheckBox);

        JPanel alignPanel = new JPanel();
        GridBagLayout alignPanelLayout = new GridBagLayout();
        alignPanel.setLayout(alignPanelLayout);
        alignPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_ALIGN" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _alignVariablesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_VAR_IDENT" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_VAR_IDENTS, ConventionDefaults.ALIGN_VAR_IDENTS));
        _alignVariablesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        alignPanelLayout.setConstraints(_alignVariablesCheckBox, c);
        alignPanel.add(_alignVariablesCheckBox);

        _alignAssignmentsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_VAR_ASSIGN" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_VAR_ASSIGNS, ConventionDefaults.ALIGN_VAR_ASSIGNS));
        _alignAssignmentsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        alignPanelLayout.setConstraints(_alignAssignmentsCheckBox, c);
        alignPanel.add(_alignAssignmentsCheckBox);

        _alignMethodDefParamsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_PARAM" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_PARAMS_METHOD_DEF,
                    ConventionDefaults.ALIGN_PARAMS_METHOD_DEF));
        _alignMethodDefParamsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        alignPanelLayout.setConstraints(_alignMethodDefParamsCheckBox, c);
        alignPanel.add(_alignMethodDefParamsCheckBox);

        _alignMethodCallChainsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_CALL_CHAINED" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_METHOD_CALL_CHAINS,
                    ConventionDefaults.ALIGN_METHOD_CALL_CHAINS));
        _alignMethodCallChainsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        alignPanelLayout.setConstraints(_alignMethodCallChainsCheckBox, c);
        alignPanel.add(_alignMethodCallChainsCheckBox);

        _alignTernaryOperatorCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_TERNARY_EXPR" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_TERNARY_OPERATOR,
                    ConventionDefaults.ALIGN_TERNARY_OPERATOR));
        _alignTernaryOperatorCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        alignPanelLayout.setConstraints(_alignTernaryOperatorCheckBox, c);
        alignPanel.add(_alignTernaryOperatorCheckBox);
        
        _alignDeclAssignmentsCheckBox =
            new JCheckBox(
                "Align Declaration Assignment",
                this.settings.getBoolean(
                    ConventionKeys.ALIGN_VAR_DECL_ASSIGNS, this.settings.getBoolean(
                            ConventionKeys.ALIGN_VAR_ASSIGNS,
                            ConventionDefaults.ALIGN_VAR_ASSIGNS)));
        _alignDeclAssignmentsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        alignPanelLayout.setConstraints(_alignDeclAssignmentsCheckBox, c);
        alignPanel.add(_alignDeclAssignmentsCheckBox);


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
        layout.setConstraints(miscPanel, c);
        panel.add(miscPanel);

        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(alignPanel, c);
        panel.add(alignPanel);

        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 5, 5);
        layout.setConstraints(continuationPanel, c);
        panel.add(continuationPanel);

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
