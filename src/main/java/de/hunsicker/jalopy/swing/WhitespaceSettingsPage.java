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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Settings page for the Jalopy printer whitespace settings.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class WhitespaceSettingsPage
    extends AbstractSettingsPage
{
    //~ Instance variables ---------------------------------------------------------------

    private JCheckBox _assignmentOperatorsCheckBox;
    private JCheckBox _bangCheckBox;
    private JCheckBox _bitwiseOperatorsCheckBox;
    private JCheckBox _bracesCheckBox;
    private JCheckBox _bracketsCheckBox;
    private JCheckBox _bracketsTypesCheckBox;
    private JCheckBox _caseColonCheckBox;
    private JCheckBox _castCheckBox;
    private JCheckBox _commaCheckBox;
    private JCheckBox _logicalOperatorsCheckBox;
    private JCheckBox _mathematicalOperatorsCheckBox;
    private JCheckBox _methodCallCheckBox;
    private JCheckBox _methodDefCheckBox;
    private JCheckBox _paddingBracesCheckBox;
    private JCheckBox _paddingBracketsCheckBox;
    private JCheckBox _paddingCastCheckBox;
    private JCheckBox _paddingParenCheckBox;
    private JCheckBox _relationalOperatorsCheckBox;
    private JCheckBox _semicolonCheckBox;
    private JCheckBox _shiftOperatorsCheckBox;
    private JCheckBox _statementCheckBox;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new WhitespaceSettingsPage object.
     */
    public WhitespaceSettingsPage()
    {
        initialize();
    }


    /**
     * Creates a new WhitespaceSettingsPage.
     *
     * @param container the parent container.
     */
    WhitespaceSettingsPage(SettingsContainer container)
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
            ConventionKeys.SPACE_AFTER_COMMA, _commaCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_AFTER_SEMICOLON, _semicolonCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_AFTER_CAST, _castCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_METHOD_CALL_PAREN,
            _methodCallCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_METHOD_DEF_PAREN, _methodDefCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN, _statementCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_CASE_COLON, _caseColonCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_BRACKETS_TYPES,
            _bracketsTypesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_BRACKETS, _bracketsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_BRACES, _bracesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.SPACE_BEFORE_LOGICAL_NOT, _bangCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_BRACKETS, _paddingBracketsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_BRACES, _paddingBracesCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_PAREN, _paddingParenCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_CAST, _paddingCastCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_MATH_OPERATORS,
            _mathematicalOperatorsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_LOGICAL_OPERATORS,
            _logicalOperatorsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_RELATIONAL_OPERATORS,
            _relationalOperatorsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_ASSIGNMENT_OPERATORS,
            _assignmentOperatorsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_SHIFT_OPERATORS, _shiftOperatorsCheckBox.isSelected());
        this.settings.putBoolean(
            ConventionKeys.PADDING_BITWISE_OPERATORS,
            _bitwiseOperatorsCheckBox.isSelected());
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        JPanel beforePanel = new JPanel();
        GridBagLayout beforeLayout = new GridBagLayout();
        beforePanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_SPACE_BEFORE" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 0)));
        beforePanel.setLayout(beforeLayout);

        GridBagConstraints c = new GridBagConstraints();

        _methodDefCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_PARENTHESES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_METHOD_DEF_PAREN,
                    ConventionDefaults.SPACE_BEFORE_METHOD_DEF_PAREN));
        _methodDefCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_methodDefCheckBox, c);
        beforePanel.add(_methodDefCheckBox);

        _methodCallCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_METHOD_CALL_PARENTHESES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_METHOD_CALL_PAREN,
                    ConventionDefaults.SPACE_BEFORE_METHOD_CALL_PAREN));
        _methodCallCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, 1, GridBagConstraints.REMAINDER, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_methodCallCheckBox, c);
        beforePanel.add(_methodCallCheckBox);

        _statementCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_STATEMENT_PARENTHESES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN,
                    ConventionDefaults.SPACE_BEFORE_STATEMENT_PAREN));
        _statementCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_statementCheckBox, c);
        beforePanel.add(_statementCheckBox);

        _bracesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BRACES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_BRACES,
                    ConventionDefaults.SPACE_BEFORE_BRACES));
        _bracesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_bracesCheckBox, c);
        beforePanel.add(_bracesCheckBox);

        _bracketsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BRACKETS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_BRACKETS,
                    ConventionDefaults.SPACE_BEFORE_BRACKETS));
        _bracketsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_bracketsCheckBox, c);
        beforePanel.add(_bracketsCheckBox);

        _bracketsTypesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BRACKETS_TYPES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_BRACKETS_TYPES,
                    ConventionDefaults.SPACE_BEFORE_BRACKETS_TYPES));
        _bracketsTypesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_bracketsTypesCheckBox, c);
        beforePanel.add(_bracketsTypesCheckBox);

        _caseColonCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_CASE_COLON" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_CASE_COLON,
                    ConventionDefaults.SPACE_BEFORE_CASE_COLON));
        _caseColonCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 3, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        beforeLayout.setConstraints(_caseColonCheckBox, c);
        beforePanel.add(_caseColonCheckBox);

        JPanel afterPanel = new JPanel();
        afterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        afterPanel.setBorder(
            BorderFactory.createTitledBorder(
                this.bundle.getString("BDR_SPACE_AFTER" /* NOI18N */)));

        _commaCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_COMMA" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA));
        _commaCheckBox.addActionListener(this.trigger);
        afterPanel.add(_commaCheckBox);

        _semicolonCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_SEMI" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_AFTER_SEMICOLON,
                    ConventionDefaults.SPACE_AFTER_SEMICOLON));
        _semicolonCheckBox.addActionListener(this.trigger);
        afterPanel.add(_semicolonCheckBox);

        _castCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_CAST" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_AFTER_CAST, ConventionDefaults.SPACE_AFTER_CAST));
        _castCheckBox.addActionListener(this.trigger);
        afterPanel.add(_castCheckBox);

        _bangCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_NEGATION" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.SPACE_BEFORE_LOGICAL_NOT,
                    ConventionDefaults.SPACE_BEFORE_LOGICAL_NOT));
        _bangCheckBox.addActionListener(this.trigger);
        afterPanel.add(_bangCheckBox);

        GridBagLayout aroundLayout = new GridBagLayout();
        JPanel aroundPanel = new JPanel();
        aroundPanel.setLayout(aroundLayout);
        aroundPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    this.bundle.getString("BDR_SPACE_AROUND" /* NOI18N */)),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));

        _assignmentOperatorsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_OPERATOR_ASSIGN" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_ASSIGNMENT_OPERATORS,
                    ConventionDefaults.PADDING_ASSIGNMENT_OPERATORS));
        _assignmentOperatorsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.RELATIVE, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_assignmentOperatorsCheckBox, c);
        aroundPanel.add(_assignmentOperatorsCheckBox);

        _bitwiseOperatorsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_OPERATOR_BITWISE" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_BITWISE_OPERATORS,
                    ConventionDefaults.PADDING_BITWISE_OPERATORS));
        _bitwiseOperatorsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_bitwiseOperatorsCheckBox, c);
        aroundPanel.add(_bitwiseOperatorsCheckBox);

        _logicalOperatorsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_OPERATOR_LOGICAL" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_LOGICAL_OPERATORS,
                    ConventionDefaults.PADDING_LOGICAL_OPERATORS));
        _logicalOperatorsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.RELATIVE, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_logicalOperatorsCheckBox, c);
        aroundPanel.add(_logicalOperatorsCheckBox);

        _mathematicalOperatorsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_OPERATOR_MATHEMATICAL" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_MATH_OPERATORS,
                    ConventionDefaults.PADDING_MATH_OPERATORS));
        _mathematicalOperatorsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_mathematicalOperatorsCheckBox, c);
        aroundPanel.add(_mathematicalOperatorsCheckBox);

        _relationalOperatorsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_OPERATOR_RELATIONAL" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_RELATIONAL_OPERATORS,
                    ConventionDefaults.PADDING_RELATIONAL_OPERATORS));
        _relationalOperatorsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.RELATIVE, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_relationalOperatorsCheckBox, c);
        aroundPanel.add(_relationalOperatorsCheckBox);

        _shiftOperatorsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_OPERATOR_SHIFT" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_SHIFT_OPERATORS,
                    ConventionDefaults.PADDING_SHIFT_OPERATORS));
        _shiftOperatorsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_shiftOperatorsCheckBox, c);
        aroundPanel.add(_shiftOperatorsCheckBox);

        _paddingBracesCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BRACES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_BRACES, ConventionDefaults.PADDING_BRACES));
        _paddingBracesCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 3, GridBagConstraints.RELATIVE, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_paddingBracesCheckBox, c);
        aroundPanel.add(_paddingBracesCheckBox);

        _paddingBracketsCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_BRACKETS" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_BRACKETS, ConventionDefaults.PADDING_BRACKETS));
        _paddingBracketsCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 3, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_paddingBracketsCheckBox, c);
        aroundPanel.add(_paddingBracketsCheckBox);

        _paddingParenCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_PARENTHESES" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_PAREN, ConventionDefaults.PADDING_PAREN));
        _paddingParenCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 0, 4, GridBagConstraints.RELATIVE, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_paddingParenCheckBox, c);
        aroundPanel.add(_paddingParenCheckBox);

        _paddingCastCheckBox =
            new JCheckBox(
                this.bundle.getString("CHK_PARENTHESES_CAST" /* NOI18N */),
                this.settings.getBoolean(
                    ConventionKeys.PADDING_CAST, ConventionDefaults.PADDING_CAST));
        _paddingCastCheckBox.addActionListener(this.trigger);
        SwingHelper.setConstraints(
            c, 1, 4, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        aroundLayout.setConstraints(_paddingCastCheckBox, c);
        aroundPanel.add(_paddingCastCheckBox);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(beforePanel, c);
        add(beforePanel);

        c.insets.top = 0;
        c.insets.bottom = 0;
        SwingHelper.setConstraints(
            c, 0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(afterPanel, c);
        add(afterPanel);

        c.insets.top = 10;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(aroundPanel, c);
        add(aroundPanel);
    }
}
